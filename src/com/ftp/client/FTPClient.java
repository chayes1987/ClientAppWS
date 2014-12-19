/*
    @author Conor Hayes
 */
package com.ftp.client;

import com.ftp.service.IFTPService;

import javax.swing.*;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FTPClient {
    private IFTPService ftpService;
    private String hostname;

    /*
        One-arg Constructor
        @param hostname The address of the Host
     */
    public FTPClient(String hostname) {
        URL wsdlUrl;
        try {
            wsdlUrl = new URL("http://" + hostname + ":8080/ftpservice?wsdl");
        } catch (MalformedURLException e) {
            return;
        }
        QName qname = new QName("http://service.ftp.com/", "FTPServiceService");
        Service service;
        try {
            service = Service.create(wsdlUrl, qname);
        } catch (WebServiceException e) {
            return;
        }
        this.hostname = hostname;
        ftpService = service.getPort(IFTPService.class);
    }

    /*
        UploadFile Method
        Handles upload to the Server
        @param file The file that they wish to upload
        @param username The username of the user requesting to upload
        @returns Successful of failed upload
     */
    public boolean uploadFile(File file, String username){
        String path = file.getPath();
        String filename = path.substring(path.lastIndexOf("\\") + 1);
        byte[] data;
        try {
            data = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            return false;
        }
        try{
            return ftpService.upload(username, filename, data);
        }catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to upload file...");
            return false;
        }
	}

    /*
        Login Method
        Handles login to the Server
        @param username The username of the user requesting access
        @returns Successful or failed login
     */
    public boolean login(String username) {
        try{
            return ftpService.login(username);
        }catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to login...");
            return false;
        }
    }

    /*
        Logout Method
        Handles Logout from the Server
        @param The username of the user requesting to logout
        @returns True as no action is taken on server side
     */
    public boolean logout(String username) {
        try{
            return ftpService.logout(username);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Unable to logout...");
            return false;
        }
    }

    /*
        DownloadFile Method
        Handles download attempts from the Server
        @param username The username of the user requesting to download
        @param filename The name of the file that they wish to download
        @return The requested file
     */
	public byte[] downloadFile(String username, String filename) {
        try{
            return ftpService.download(username, filename);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Unable to download file...");
            return null;
        }
	}

    /*
        GetListFiles Method
        Gets all the files of the user
        @param username The username of the user requesting their files
        @return The users' files
     */
    public Object[] getListFiles(String username){
        try{
            return ftpService.getListFiles(username);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Unable to get file listing...");
            return null;
        }
    }

    /*
        SaveFile Method
        Handles saving files locally after download
        @param path The path to which to save the file to
        @param file The file to save
        @return Successful or failed save
     */
    public boolean saveFile(String path, byte[] file) {
        try {
            Files.write(Paths.get(path), file);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /*
        GetFTPService Method
        Gets the current ftpService instance, used for login
        @return The current instance of ftpService
     */
    public IFTPService getFtpService() { return ftpService; }
}
