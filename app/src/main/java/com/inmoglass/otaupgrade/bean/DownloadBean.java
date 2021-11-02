package com.inmoglass.otaupgrade.bean;


import java.io.Serializable;


public class DownloadBean implements Serializable {

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "DemoBean{" +
                "msg='" + msg + '\'' +
                ", data=" + data +
                ", code='" + code + '\'' +
                ", token=" + token +
                '}';
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getToken() {
        return token;
    }

    public void setToken(Object token) {
        this.token = token;
    }

    /**
     * msg : 成功
     * data : {"apkName":"演示环境包","versionNumber":"V1.03","model":"X","versionIdentification":"Release","platformCode":"A","downloadUrl":"https://ossspuothers-business.oss-cn-shenzhen.aliyuncs.com/apk/eef28f74-d77b-4a0f-ae87-1456ddbd3d98.apk","createTime":"20211009110429","note":"1.优化参数&字典缓存操作\r\n2.新增表格参数（导出方式&导出文件类型）\r\n3.新增表格示例（自定义视图分页）\r\n4.新增示例（表格列拖拽）\r\n5.集成yuicompressor实现(CSS/JS压缩)\r\n6.新增表格参数（是否支持打印页面showPrint）\r\n7.支持bat脚本执行应用\r\n8.修复存在的SQL注入漏洞问题"}
     * code : 200
     * token : null
     */

    private String msg;
    private DataBean data;
    private String code;
    private Object token;

    public static class DataBean implements Serializable {
        public String getApkName() {
            return apkName;
        }

        public void setApkName(String apkName) {
            this.apkName = apkName;
        }

        public String getVersionNumber() {
            return versionNumber;
        }

        public void setVersionNumber(String versionNumber) {
            this.versionNumber = versionNumber;
        }

        public String getModel() {
            return model;
        }

        @Override
        public String toString() {
            return "DataBean{" +
                    "apkName='" + apkName + '\'' +
                    ", versionNumber='" + versionNumber + '\'' +
                    ", model='" + model + '\'' +
                    ", versionIdentification='" + versionIdentification + '\'' +
                    ", platformCode='" + platformCode + '\'' +
                    ", downloadUrl='" + downloadUrl + '\'' +
                    ", createTime='" + createTime + '\'' +
                    ", note='" + note + '\'' +
                    '}';
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getVersionIdentification() {
            return versionIdentification;
        }

        public void setVersionIdentification(String versionIdentification) {
            this.versionIdentification = versionIdentification;
        }

        public String getPlatformCode() {
            return platformCode;
        }

        public void setPlatformCode(String platformCode) {
            this.platformCode = platformCode;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        /**
         * apkName : 演示环境包
         * versionNumber : V1.03
         * model : X
         * versionIdentification : Release
         * platformCode : A
         * downloadUrl : https://ossspuothers-business.oss-cn-shenzhen.aliyuncs.com/apk/eef28f74-d77b-4a0f-ae87-1456ddbd3d98.apk
         * createTime : 20211009110429
         * note : 1.优化参数&字典缓存操作
         2.新增表格参数（导出方式&导出文件类型）
         3.新增表格示例（自定义视图分页）
         4.新增示例（表格列拖拽）
         5.集成yuicompressor实现(CSS/JS压缩)
         6.新增表格参数（是否支持打印页面showPrint）
         7.支持bat脚本执行应用
         8.修复存在的SQL注入漏洞问题
         */

        private String apkName;
        private String versionNumber;
        private String model;
        private String versionIdentification;
        private String platformCode;
        private String downloadUrl;
        private String createTime;
        private String note;
    }
}

