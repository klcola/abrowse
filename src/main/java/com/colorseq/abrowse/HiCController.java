package com.colorseq.abrowse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

@Controller
public class HiCController {

    @Value("${abrowse.HiC.resultPaht}")
    String HiCResultPath;

    @Value("${abrowse.HiC.sourcePath}")
    String HiCSourcePath;

    @RequestMapping(value = "/HiC/init", method = RequestMethod.GET)
    public String showHiC(Map<String, Object> modelMap,HttpServletRequest request) throws IOException, InterruptedException {
        String chrName = request.getParameter("chrName");
        String startStr = request.getParameter("start");
        String endStr = request.getParameter("end");
        String size = request.getParameter("size");
        String path = ResourceUtils.getURL("classpath:").getPath();
        System.out.println(path);
        String title = chrName + ":" + startStr + "-" + endStr;

        chrName = chrName.replace("chr","");

        if (size == null){
            size = "20000";
        }
        int start = Integer.parseInt(startStr);
        int end = Integer.parseInt(endStr);
        int startd = (int) Math.ceil(start/20000);
        int endd = (int) Math.ceil(end/20000);


        String cmd = "python " + HiCSourcePath + "HiCPlotter.py " +
                "-f " + HiCSourcePath + "normalbrain_0508_20000.matrix " +
                "-tri 1 -bed " + HiCSourcePath + "normalbrain_0508_20000_abs.bed " +
                "-chr " + chrName + " -s " + startd + " -e " + endd + " " +
                "-n " + title + " -o " + HiCResultPath +"example2" + chrName + startStr + endStr ;
        System.out.println(cmd);
        Process proc = Runtime.getRuntime().exec(cmd);
        int i = proc.waitFor();
        System.out.println(i);
        BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String line = null;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        in.close();
        modelMap.put("imgPath","/HiC/example2" + chrName + startStr + endStr + ".png");
        return "show_HiC";
    }

    @RequestMapping(value = "/HiC/{imageName}", method = RequestMethod.GET)
    public void hicImage(@PathVariable String imageName, HttpServletResponse response) throws IOException {
        File image = new File(HiCResultPath + imageName);
        FileInputStream fileInputStream = new FileInputStream(image + ".png");
        ServletOutputStream outputStream = response.getOutputStream();
        //创建存放文件内容的数组
        byte[] buff =new byte[1024];
        //所读取的内容使用n来接收
        int n;
        //当没有读取完时,继续读取,循环
        while((n=fileInputStream.read(buff))!=-1){
            //将字节数组的数据全部写入到输出流中
            outputStream.write(buff,0,n);
        }
        outputStream.flush();
        outputStream.close();
        fileInputStream.close();
    }
}
