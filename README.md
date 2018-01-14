# DocumentView

#### Introduction
```
This is a component used to browse documents for Android Developers.  
If any issues,please leave a comment and I will deal with it as soon as possible. 

本库用于文档浏览，包括常见office文档(word,ppt,excel,pdf...)、常规文本文档、图片，支持在线文档、Asserts文档和本地文档，具体支持格式如下。

```

#### Supported file types:
```
Open in document format, Plugin will be down for the first time.
.doc
.docx
.ppt
.pptx
.xls
.xlsx
.pdf
.epub
.chm
Open in web format.
.txt
.ini
.log
.bat
.php
.js
.lrc
.html
.htm
.xml
.mht
.gif
.url
Open in image format.
.jpg
.jpeg
.png
.bmp
```

#### Directions:
```
allprojects { 
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
dependencies {  
    compile 'com.github.deparse:DocumentView:1.0.1'
}
```

#### Thanks to:
```
https://x5.tencent.com/tbs/
https://github.com/boycy815/PinchImageView
```
