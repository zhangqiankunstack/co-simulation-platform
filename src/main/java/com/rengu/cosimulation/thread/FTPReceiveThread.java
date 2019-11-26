/*
package com.rengu.cosimulation.thread;

*/
/**
 * Author: XYmar
 * Date: 2019/11/17 17:04
 *//*

public class FTPReceiveThread {
    private static String host = "192.168.1.129";
    private static String user = "ftpuser";
    private static String password = "ftpuser";
    private static String directory = "/var/ftp/pub";
    private static String saveFile = "E:/saveftp";

    */
/**
     * 获取FTPCLIENT
     *//*

   /private FTPClient getFtpClient() throws Exception {
        FTPClient ftp = new FTPClient();
        // 连接
        ftp.connect(host);// 连接FTP服务器
        ftp.login(user,password);// 登陆FTP服务器
        //验证FTP服务器是否登录成功
        int replyCode = ftp.getReplyCode();
        if(!FTPReply.isPositiveCompletion(replyCode)){
            System.out.println("登录验证失败");
        }
        ftp.setControlEncoding("UTF-8"); // 中文支持
        ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
//		ftp.enterLocalPassiveMode();  // 被动模式
        ftp.enterLocalActiveMode();  // 主动模式
        ftp.changeWorkingDirectory(directory);
        return ftp;
    }

    *
            * FTP下载文件

    public String download() {
        OutputStream os = null;
        String result = "";
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient = getFtpClient();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("FTP连接发生异常！", e);
        }
        try {
            //切换FTP目录
            ftpClient.changeWorkingDirectory(directory);
            FTPFile[] ftpFiles = ftpClient.listFiles();
            //遍历目录下所有文件
            for(FTPFile file : ftpFiles){
                File localFile = new File(saveFile + File.separator + file.getName());
                os = new FileOutputStream(localFile);
                ftpClient.retrieveFile(file.getName(), os);
                os.close();
            }
            System.out.println("ftp dowmload over");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("FTP客户端出错！", e);
        } finally {
            try {
                os.close();
                ftpClient.logout();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("关闭FTP连接发生异常！", e);
            }
        }
        return result;
    }
}
*/
