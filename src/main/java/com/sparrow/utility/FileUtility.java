/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sparrow.utility;

import com.sparrow.constant.CONSTANT;
import com.sparrow.constant.EXTENSION;
import com.sparrow.constant.FILE;
import com.sparrow.constant.magic.DIGIT;
import com.sparrow.constant.magic.SYMBOL;
import com.sparrow.support.EnvironmentSupport;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author harry
 */
public class FileUtility {

    private static Logger logger = LoggerFactory.getLogger(FileUtility.class);
    private static FileUtility fileUtil = new FileUtility();

    private FileUtility() {
    }

    public static FileUtility getInstance() {
        return fileUtil;
    }

    public String readFileContent(String fileName) {
        return readFileContent(fileName, null);
    }

    public List<String> readLines(String fileName) {
        return this.readLines(fileName, CONSTANT.CHARSET_UTF_8);
    }

    public List<String> readLines(InputStream inputStream) {
        return this.readLines(inputStream, CONSTANT.CHARSET_UTF_8);
    }

    public List<String> readLines(InputStream inputStream, String charset) {

        List<String> fileLines = new ArrayList<String>();
        if (StringUtility.isNullOrEmpty(charset)) {
            charset = CONSTANT.CHARSET_UTF_8;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(
                inputStream, charset));
            String tempString;
            while ((tempString = reader.readLine()) != null) {
                fileLines.add(tempString);
            }
        } catch (IOException e) {
            logger.error("flush error", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.error("close error", e);
                }
            }
        }
        return fileLines;
    }

    public List<String> readLines(String fileName, String charset) {
        if (StringUtility.isNullOrEmpty(charset)) {
            charset = CONSTANT.CHARSET_UTF_8;
        }
        return this.readLines(EnvironmentSupport.getInstance().getFileInputStream(fileName), charset);
    }

    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public String readFileContent(String fileName, String charset) {
        if (StringUtility.isNullOrEmpty(charset)) {
            charset = CONSTANT.CHARSET_UTF_8;
        }
        InputStream inputStream = EnvironmentSupport.getInstance().getFileInputStream(fileName);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = -1;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, length);
            }
            return bos.toString(charset);
        } catch (IOException e) {
            logger.error("write error", e);
            return SYMBOL.EMPTY;
        } finally {
            try {
                bos.close();
                inputStream.close();
            } catch (IOException ignore) {
            }
        }
    }

    public boolean writeFile(String filePath, String s) {
        String charset = CONSTANT.CHARSET_UTF_8;
        return writeFile(filePath, s, charset);
    }

    private String makeDirectory(String fullFilePath) {
        File destFile = new File(fullFilePath);
        if (destFile.isDirectory()) {
            return fullFilePath;
        }
        int index = fullFilePath.lastIndexOf(File.separator);
        String descDirectoryPath = fullFilePath.substring(DIGIT.ZERO, index + DIGIT.ONE);
        if (StringUtility.isNullOrEmpty(descDirectoryPath)) {
            descDirectoryPath = System.getProperty("user.dir");
            fullFilePath = descDirectoryPath + File.separator + fullFilePath;
        }
        // 判断并创建原图路径
        File descDirectory = new File(descDirectoryPath);
        if (!descDirectory.exists()) {
            if (descDirectory.mkdirs()) {
                return fullFilePath;
            }
        }
        return fullFilePath;
    }

    public boolean writeFile(String filePath, String s, String charset) {
        OutputStreamWriter osw = null;
        try {
            //先判断class下是否存在
            String fileFullPath = EnvironmentSupport.getInstance().getClassesPhysicPath() + filePath;
            if (!new File(fileFullPath).exists()) {
                //不存在则新建class以外的目录
                fileFullPath = this.makeDirectory(filePath);
            }
            OutputStream outputStream = new FileOutputStream(fileFullPath);
            osw = new OutputStreamWriter(outputStream,
                charset);
            osw.write(s, DIGIT.ZERO, s.length());
            osw.flush();
            return true;
        } catch (Exception e) {
            logger.error("flush error", e);
            return false;
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    logger.error("close error", e);
                }
            }
        }
    }

    /**
     * 文件复制
     *
     * @param descFilePath
     * @param srcFilePath
     */
    public void copy(String srcFilePath, String descFilePath) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            String fileFullPath = this.makeDirectory(descFilePath);
            File descFile = new File(fileFullPath);
            if (descFile.isDirectory()) {
                descFile = new File(fileFullPath + File.separator + FileUtility.getInstance().getFileNameWithExtension(srcFilePath));
            }
            is = new FileInputStream(srcFilePath);
            fos = new FileOutputStream(descFile);
            byte[] buffer = new byte[DIGIT.K];
            while (true) {
                int readSize = is.read(buffer);
                if (readSize == -1) {
                    break;
                }
                fos.write(buffer, DIGIT.ZERO, readSize);
            }

        } catch (Exception e) {
            throw new RuntimeException("file not found", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("input stream error", e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    logger.error("output stream error", e);
                }
            }
        }
    }

    /**
     * 文件复制
     *
     * @param inputStream
     * @param outputStream
     */
    public void copy(InputStream inputStream, OutputStream outputStream) {
        if (inputStream == null || outputStream == null) {
            return;
        }
        try {
            byte[] buffer = new byte[DIGIT.K];
            while (true) {
                int readSize = inputStream.read(buffer);
                if (readSize == -1) {
                    break;
                }
                outputStream.write(buffer, DIGIT.ZERO, readSize);
            }
            if (outputStream instanceof ZipOutputStream) {
                ((ZipOutputStream) outputStream).finish();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("input stream close error", e);
            }
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                logger.error("output stream close error", e);
            }
        }
    }

    public String search(String path, String keyword, int skip,
        Comparator<String> compare, int minSkip) {
        File file = new File(path);

        BufferedReader reader = null;
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new FileNotFoundException(file.getPath());
            }
            reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), CONSTANT.CHARSET_UTF_8), skip);
            String tempString;
            reader.mark(skip);
            skip /= 2;
            reader.skip(skip);
            reader.readLine();
            boolean isSkip = true;
            String lastWord = SYMBOL.EMPTY;
            while ((tempString = reader.readLine()) != null) {
                if (compare.compare(tempString, keyword) == 0) {
                    return tempString;
                } else if (tempString.equals(lastWord)) {
                    return SYMBOL.EMPTY;
                }
                // 是否需要skip
                if (!isSkip) {
                    continue;
                }
                // 往外跳
                if (compare.compare(tempString, keyword) < DIGIT.ZERO) {
                    // 先做标记
                    reader.mark(skip);
                    // 如果不足标准skip ，跳跃浮度减半
                    if (skip < skip / DIGIT.TOW) {
                        skip /= DIGIT.TOW;
                    } else {
                        // 跳出文本范围 则减半
                        while (reader.skip(skip) < skip) {
                            skip /= DIGIT.TOW;
                            reader.reset();
                        }
                    }
                    reader.readLine();
                    // 向里跳
                } else {
                    reader.reset();
                    skip /= DIGIT.TOW;
                    if (skip <= minSkip) {
                        isSkip = false;
                        lastWord = tempString;
                    } else {
                        reader.skip(skip);
                        reader.readLine();
                    }
                }
            }
            return SYMBOL.EMPTY;
        } catch (IOException e) {
            logger.error("reade file error", e);
            return SYMBOL.EMPTY;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    logger.error("reader close error", e1);
                }
            }
        }
    }

    /**
     * 根据单位为KB的长度获取文件长度（可变单位为MB GB）
     *
     * @param length 单位是KB
     * @return
     */
    public String getFileLength(double length) {
        // 获取其整数部分
        if (length < DIGIT.ONE) {
            return String.valueOf(Math.ceil(length * DIGIT.K)) + "B";
        }
        // 不小于这个数的最小整数
        if (length >= DIGIT.ONE && length < DIGIT.K) {
            return String.valueOf(Math.ceil(length)) + "KB";
        }
        // 四舍五入保留两位小数
        if (length >= DIGIT.K && length < Math.pow(DIGIT.K, DIGIT.TOW)) {
            return Math.ceil(length / DIGIT.K * DIGIT.HUNDRED) / DIGIT.HUNDRED + "MB";
        }
        // 四舍五入保留两位小数
        return Math.ceil(length / DIGIT.K / DIGIT.K * DIGIT.HUNDRED) / DIGIT.HUNDRED + "GB";
    }

    /**
     * 获取文件扩展名
     *
     * @param fileUrl
     * @return .jpg
     */
    public String getExtension(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf('.'))
            .toLowerCase();
    }

    public String getImageExtension(String fileUrl) {
        String extension = this.getExtension(fileUrl);
        String imageExtensionConfig = Config.getValue(FILE.IMAGE_EXTENSION);
        if (imageExtensionConfig == null) {
            return extension;
        }
        String[] imageExtension = imageExtensionConfig
            .split("\\|");
        // jpeg 或者是其他格式都转换成jpg
        if (EXTENSION.JPEG.equalsIgnoreCase(extension)
            || !StringUtility.existInArray(imageExtension, extension)) {
            extension = EXTENSION.JPG;
        }
        return extension;
    }

    public String getFileNameWithExtension(String fileUrl) {
        // 获取客户端文件名 http://img.zhuaququ.com/0/0.100.jpg?date=new date()
        int fileNameStartIndex = fileUrl.lastIndexOf('/') + DIGIT.ONE;
        return fileUrl.substring(fileNameStartIndex);
    }

    /**
     * 获取文件名 不包含扩展名
     *
     * @param fulleFilePath
     * @return
     */
    public String getFileName(String fulleFilePath) {
        // 获取客户端文件名 http://img.zhuaququ.com/0/0.100.jpg?date=new date()
        int fileNameStartIndex = fulleFilePath.lastIndexOf('/') + DIGIT.ONE;
        int fileNameEndIndex = fulleFilePath.lastIndexOf('.');
        return fulleFilePath.substring(fileNameStartIndex,
            fileNameEndIndex);
    }

    /**
     * 通过一数字ID获取文件打散路径
     *
     * @param id
     * @param extension
     * @param isWebPath
     * @param size
     * @return
     */
    public String getBreakUpPath(long id, String extension, boolean isWebPath,
        String size) {
        boolean isImage = this.isImage(extension);
        long remaining = id % DIGIT.TEN;
        long remaining1 = id % DIGIT.THOUSAND;
        long div = id / DIGIT.THOUSAND;
        long remaining2 = div % DIGIT.THOUSAND;
        String path = "";
        if (isImage) {
            if (isWebPath) {
                path = Config.getValue(FILE.PATH.IMG_URL)
                    + "/%2$s/%3$s/%4$s/%5$s%6$s";
            } else {
                path = Config.getValue(FILE.PATH.IMG_UNC)
                    + "/%2$s/%3$s/%4$s/%5$s%6$s";
            }
            return String.format(path, remaining, size, remaining2, remaining1,
                id, extension);
        }
        path = Config.getValue(FILE.PATH.FILE_UNC)
            + "/%1$s/%2$s/%3$s%4$s";
        return String.format(path, remaining2, remaining1, id, extension);
    }

    public String getBreakUpPath(String uuid) {
        int id = Math.abs(uuid.hashCode());
        long remaining = id % DIGIT.TEN;
        long remaining1 = id % DIGIT.THOUSAND;
        long div = id / DIGIT.THOUSAND;
        long remaining2 = div % DIGIT.THOUSAND;
        return String.format("%1$s/%2$s/%3$s/%4$s", remaining, remaining2, remaining1, uuid);
    }

    public boolean isImage(String extension) {
        return StringUtility.existInArray(Config
            .getValue(FILE.IMAGE_EXTENSION).split("\\|"), extension);
    }

    /**
     * 根据站内附件url获取物理路径
     *
     * @param filePath
     * @param size
     * @return
     */
    public String getPhysicalPath(String filePath, String size) {
        String fileId = this.getFileName(filePath);
        String extension = this.getImageExtension(filePath);
        return this.getBreakUpPath(Integer.valueOf(fileId), extension, false,
            size);
    }

    public void delete(String path, long beforeMillis) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        // 如果没有文件
        if (files.length == DIGIT.ZERO) {
            file.delete();
            return;
        }
        for (File f : files) {
            if (f.isDirectory() && !f.isHidden()) {
                delete(f.getPath(), beforeMillis);
                continue;
            }
            if (f.lastModified() < beforeMillis) {
                f.delete();
            }
        }
    }

    /**
     * 删除文件
     */
    public void deleteByFileId(Long fileId, String clientFileName) {
        String extension = this.getExtension(clientFileName);
        Boolean isImage = this.isImage(extension);
        if (this.isImage(extension)) {
            String imageExtension = this.getImageExtension(clientFileName);
            String imageFullPath = FileUtility.getInstance().getBreakUpPath(
                fileId,
                imageExtension, false, FILE.SIZE.ORIGIN);
            File origin = new File(imageFullPath);
            if (origin.exists()) {
                origin.delete();
            }

            File big = new File(imageFullPath.replace(FILE.SIZE.ORIGIN,
                FILE.SIZE.BIG));
            if (big.exists()) {
                big.delete();
            }

            File middle = new File(imageFullPath.replace(FILE.SIZE.ORIGIN,
                FILE.SIZE.MIDDLE));
            if (middle.exists()) {
                middle.delete();
            }

            File small = new File(imageFullPath.replace(FILE.SIZE.ORIGIN,
                FILE.SIZE.SMALL));
            if (small.exists()) {
                small.delete();
            }
        } else {
            String attachFileFullName = FileUtility.getInstance().getBreakUpPath(
                fileId, extension, false, FILE.SIZE.ATTACH);
            File origin = new File(attachFileFullName);
            if (origin.exists()) {
                origin.delete();
            }
        }
    }
}