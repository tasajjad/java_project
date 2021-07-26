package project;

public class Main {
    public static void main(String[] args) {
        final String downloadDir="./";
        final String url="https://www.sample-videos.com/video/mp4/";

        DownloadManager downloadManager =new DownloadManager();
        downloadManager.download(url,downloadDir);
    }
}
