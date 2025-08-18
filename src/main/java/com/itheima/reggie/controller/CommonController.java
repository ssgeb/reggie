package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Filter;

/**
 * 文件上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {


    @Value("${reggie.path}")
    private String basepath;

    @PostMapping("/upload")
    public R <String> upload(MultipartFile file){
      log.info("文件上传");

      String originalFilename=file.getOriginalFilename();
      String suffix=originalFilename.substring(originalFilename.lastIndexOf('.'));
      String filename=UUID.randomUUID().toString() + suffix;
      File dir =new File(basepath);
      if (!dir.exists()) {
          //目录不存在
          dir.mkdirs();
      }
        try {
            file.transferTo(new File(basepath+filename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(filename);
    }
    @GetMapping("/download")
    public  void download(String name, HttpServletResponse response){
            
        //输入流，读取数据
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basepath+name));
            ServletOutputStream outputStream = response.getOutputStream();
            int len=0;
            byte[] bytes=new byte[1024];
            while ((len=fileInputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            fileInputStream.close();
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
