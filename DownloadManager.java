package project;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    public void download(String url,String downloadDir){
        if(!isValidUrl(url)){
            throw new IllegalArgumentException("This url not valid");
        }
        try{
            URL urlToDownload=new URL(url);
            long totalContentSize= getDownloadableContentSize(urlToDownload);

            int nChunk=getTotalChunk();

            long partSize=totalContentSize/nChunk;
            long remainingBytes= totalContentSize%nChunk;

            ExecutorService threadPool= Executors.newFixedThreadPool(nChunk);

            CountDownLatch countDownLatch= new CountDownLatch(nChunk);


            long startByte=0;
            for(int chunk=0;chunk<nChunk;chunk++){
                long byteCount=partSize;
                if(byteCount==(nChunk-1)){
                    byteCount+=remainingBytes;
                }
                String partsName=getPartsName(nChunk);
                DownloadTask downloadTask=new DownloadTask(urlToDownload,startByte,byteCount,partsName,downloadDir,countDownLatch);
                startByte=startByte+partSize+1;
                threadPool.submit(downloadTask);
            }
            String fileName=url.substring(url.lastIndexOf('/')+1,url.length());

            threadPool.submit(new MergeTask(fileName,downloadDir,countDownLatch,nChunk));
            threadPool.shutdown();
            threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        }catch (IOException | InterruptedException e){
            System.out.println("Couldn`t not downlaod, -"+e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    private String getPartsName(int chunkId){
        return chunkId +Constants.PART_EXTENSION;

    }
    private long getDownloadableContentSize(URL urlToDownload)throws IOException{
        return urlToDownload.openConnection().getContentLength();
    }
    private int getTotalChunk(){
        return Runtime.getRuntime().availableProcessors();
    }
    private static boolean isValidUrl(String url){
        try{
            new URL(url).toURI();
        }catch (URISyntaxException| MalformedURLException exception){
            return false;
        }
    }
}
