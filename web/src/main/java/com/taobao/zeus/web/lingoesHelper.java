package com.taobao.zeus.web;

import com.taobao.zeus.util.JsonUtil;
import com.taobao.zeus.web.util.DataX.TxtFileReader.Column;
import com.taobao.zeus.web.util.DataX.TxtFileReader.Parameter;
import com.taobao.zeus.web.util.DataX.TxtFileReader.Reader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/lingoesHelper.do")
public class lingoesHelper extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();

        Map<String, String> params = new HashMap<>();
        try {


            Enumeration em = request.getParameterNames();
            while (em.hasMoreElements()) {
                String name = (String) em.nextElement();
                String value = request.getParameter(name);
                params.put(name, value);
            }
            if (params.containsKey("srcTxtFile")) {
                Reader reader = new Reader();
                reader.setName("txtfilereader");
                Parameter parameter = new Parameter();
                parameter.setPath(Arrays.asList(params.get("srctxtFilePath").split(",")));
                parameter.setCompress(params.get("srctxtCompressFormat"));
                parameter.setFieldDelimiter(params.get("srctxtFieldDelimiter"));
                parameter.setSkipHeader(params.get("srctxtSkipHeader"));

                List<Column> column = new ArrayList<>();
                Column item = new Column();
                item.setIndex(0);
                item.setType("string");
                column.add(item);

                parameter.setColumn(column);
                reader.setParameter(parameter);

                String ret = JsonUtil.bean2json(reader);
                out.write(ret);
                out.flush();

            } else if (params.containsKey("srcFtpFile")) {
                com.taobao.zeus.web.util.DataX.FtpFileReader.Reader reader=new com.taobao.zeus.web.util.DataX.FtpFileReader.Reader();
                reader.setName("ftpreader");
                com.taobao.zeus.web.util.DataX.FtpFileReader.Parameter parameter = new com.taobao.zeus.web.util.DataX.FtpFileReader.Parameter();
                parameter.setHost(params.get("srcftpHost"));
                parameter.setPort(Integer.valueOf(params.get("srcFtpPort")));
                parameter.setUsername(params.get("srcFtpUserName"));
                parameter.setPassword(params.get("srcFtpPassword"));
                parameter.setPath(Arrays.asList(params.get("srcFtpFilePath").split(",")));
                parameter.setFieldDelimiter(params.get("srcFtpFieldDelimiter"));
                parameter.setCompress(params.get("srcFtpCompressFormat"));
                parameter.setSkipHeader(params.get("srcftpSkipHeader"));

                List<com.taobao.zeus.web.util.DataX.FtpFileReader.Column> column = new ArrayList<>();
                com.taobao.zeus.web.util.DataX.FtpFileReader.Column item = new com.taobao.zeus.web.util.DataX.FtpFileReader.Column();
                item.setIndex(0);
                item.setType("string");
                column.add(item);
                parameter.setColumn(column);

                reader.setParameter(parameter);

                String ret = JsonUtil.bean2json(reader);
                out.write(ret);
                out.flush();

            } else if (params.containsKey("srcHiveFile")) {
                com.taobao.zeus.web.util.DataX.HiveReader.Reader reader=new com.taobao.zeus.web.util.DataX.HiveReader.Reader();
                reader.setName("hdfsreader");
                com.taobao.zeus.web.util.DataX.HiveReader.Parameter parameter = new com.taobao.zeus.web.util.DataX.HiveReader.Parameter();
                parameter.setPath(params.get("srcHiveFilePath"));
                parameter.setFieldDelimiter(params.get("srcHiveFieldDelimiter"));
                parameter.setFileType(params.get("srcHiveFileFormat"));
                parameter.setDefaultFS("");//TODO
                parameter.setHaveKerberos("true");
                parameter.setKerberosKeytabFilePath("");//TODO
                parameter.setKerberosPrincipal("");//TODO

                List<com.taobao.zeus.web.util.DataX.HiveReader.Column> column = new ArrayList<>();
                com.taobao.zeus.web.util.DataX.HiveReader.Column item = new com.taobao.zeus.web.util.DataX.HiveReader.Column();
                item.setIndex(0);
                item.setType("string");
                column.add(item);

                parameter.setColumn(column);
                reader.setParameter(parameter);

                String ret = JsonUtil.bean2json(reader);
                out.write(ret);
                out.flush();

            } else if (params.containsKey("tarHive")) {

            } else if (params.containsKey("tarMysql")) {

            } else if (params.containsKey("tarOracle")) {

            }
        } catch (Exception e) {

        }finally {
            if (out != null) {
                out.close();
            }
        }
    }

}