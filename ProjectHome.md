## Spring MVC Multipart Upload support for GAE ##
### Version History: ###
  * _06-Mar-2012_ **v0.4** - [download](http://gmultipart.googlecode.com/svn/repo/m2/gmultipart/gmultipart/0.4/gmultipart-0.4.jar)
    * supports Spring 3.1(>= Spring 3.1)
    * compiled in Java6
    * [Issue-4](http://code.google.com/p/gmultipart/issues/detail?id=4) fixed _(Thanks to kctang@big2.net for the patch.)_

  * _02-June-2011_ **v0.3** - [download](http://gmultipart.googlecode.com/svn/repo/m2/gmultipart/gmultipart/0.3/gmultipart-0.3.jar)
    * supports Spring 3.0 (>= Spring 3.0 RC1)
    * compiled in Java6
    * uses maven2 build (no more ant build support) - [Issue-3](http://code.google.com/p/gmultipart/issues/detail?id=3) fixed.

  * _12-Jan-2010_ **v0.2-java5** - [download](http://gmultipart.googlecode.com/files/gmultipart-0.2-java5.jar)
    * supports Spring 3.0 (>= Spring 3.0 RC1)
    * Compiled in Java5 - [Issue-2](http://code.google.com/p/gmultipart/issues/detail?id=2) fixed
    * Uses ant build _(Thanks to [pmatiello](http://code.google.com/u/pmatiello/) for the ant build file)_

  * _06-Oct-2009_ **v0.2** - [download](http://gmultipart.googlecode.com/files/gmultipart-0.2.jar)
    * Supports Spring 3.0 (>= Spring 3.0 RC1)
    * Compiled in Java6
    * [Issue-1](http://code.google.com/p/gmultipart/issues/detail?id=1) fixed

  * _12-Sept-2009_ **v0.1** - [download](http://gmultipart.googlecode.com/files/gmultipart-0.1.jar)
    * supports Spring 2.x (< Spring 3.0 RC1)
    * Compiled in Java6

### Overview: ###
This implementation removes all the file handling related codes from the following files to make it work in Google App Engine.

  1. org.springframework.web.multipart.commons
    * [CommonsFileUploadSupport.java](http://static.springsource.org/spring/docs/2.5.x/api/org/springframework/web/multipart/commons/CommonsFileUploadSupport.html) --> [GFileUploadSupport.java](http://code.google.com/p/gmultipart/source/browse/trunk/src/main/java/org/gmr/web/multipart/GFileUploadSupport.java)
    * [CommonsMultipartFile.java](http://static.springsource.org/spring/docs/2.5.x/api/org/springframework/web/multipart/commons/CommonsMultipartFile.html) --> [GMultipartFile.java](http://code.google.com/p/gmultipart/source/browse/trunk/src/main/java/org/gmr/web/multipart/GMultipartFile.java)
    * [CommonsMultipartResolver.java](http://static.springsource.org/spring/docs/2.5.x/api/org/springframework/web/multipart/commons/CommonsMultipartResolver.html) --> [GMultipartResolver.java](http://code.google.com/p/gmultipart/source/browse/trunk/src/main/java/org/gmr/web/multipart/GMultipartResolver.java)
  1. org.apache.commons.fileupload.disk
    * [DiskFileItemFactory.java](http://commons.apache.org/fileupload/apidocs/org/apache/commons/fileupload/disk/DiskFileItemFactory.html) --> [GFileItemFactory.java](http://code.google.com/p/gmultipart/source/browse/trunk/src/main/java/org/gmr/web/multipart/GFileItemFactory.java)
    * [DiskFileItem.java](http://commons.apache.org/fileupload/apidocs/org/apache/commons/fileupload/disk/DiskFileItem.html) --> [GFileItem.java](http://code.google.com/p/gmultipart/source/browse/trunk/src/main/java/org/gmr/web/multipart/GFileItem.java)
  1. org.apache.commons.io.output
    * [DeferredFileOutputStream.java](http://commons.apache.org/io/api-release/org/apache/commons/io/output/DeferredFileOutputStream.html) --> [GOutputStream.java](http://code.google.com/p/gmultipart/source/browse/trunk/src/main/java/org/gmr/web/multipart/GOutputStream.java)

### Maven Settings: ###
```
<repositories>
    <repository>
        <id>gmultipart</id>
        <url>http://gmultipart.googlecode.com/svn/repo/m2</url>
    </repository>
</repositories>
```

```
<dependencies>
    <dependency>
        <groupId>gmultipart</groupId>
        <artifactId>gmultipart</artifactId>
        <version>0.4</version>
    </dependency>
</dependencies>
```
### Demo: ###
  1. Edit your dispatcher-servlet.xml and add bean [org.gmr.web.multipart.GMultipartResolver](http://code.google.com/p/gmultipart/source/browse/trunk/src/main/java/org/gmr/web/multipart/GMultipartResolver.java) as shown below._**Note**_: All the property settings that can be done in [org.springframework.web.multipart.commons.CommonsMultipartResolver](http://static.springsource.org/spring/docs/2.5.x/api/org/springframework/web/multipart/commons/CommonsMultipartResolver.html), can also be done in [org.gmr.web.multipart.GMultipartResolver](http://code.google.com/p/gmultipart/source/browse/trunk/src/main/java/org/gmr/web/multipart/GMultipartResolver.java), except setting maxInMemorySize property.
```
<!--
In this implementation, maxInMemorySize is defaulted to maxUploadSize property value.
-->
<bean id="multipartResolver" class="org.gmr.web.multipart.GMultipartResolver">
    <property name="maxUploadSize" value="1048576" />
</bean>
```
  1. Lets consider this simple multipart html form.
```
<html>
  <body>
    <form action="/save" method="post" enctype="multipart/form-data">
      <input name="comment">
      <input name="file1" type="file">
      <input name="file2" type="file">
      <input name="file3" type="file">
      <input name="file4" type="file">
      <input name="file5" type="file">
    </form>
  </body>
</html>
```
  1. Create a **CommentForm.java** to represent HTML form input fields 'comment', 'file1', 'file2',  'file3', 'file4', 'file5'.
```
public class CommentForm {
	private String comment;
	private MultipartFile file1;
	private MultipartFile file2;
	private MultipartFile file3;
	private MultipartFile file4;
	private MultipartFile file5;

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setFile1(MultipartFile file1) {
		this.file1 = file1;
	}

	public void setFile2(MultipartFile file2) {
		this.file2 = file2;
	}

	public void setFile3(MultipartFile file3) {
		this.file3 = file3;
	}

	public void setFile4(MultipartFile file4) {
		this.file4 = file4;
	}

	public void setFile5(MultipartFile file5) {
		this.file5 = file5;
	}

	public MultipartFile[] getFiles() {
		List<MultipartFile> files = new ArrayList<MultipartFile>();
		if (file1 != null) {
			files.add(file1);
		}
		if (file2 != null) {
			files.add(file2);
		}
		if (file3 != null) {
			files.add(file3);
		}
		if (file4 != null) {
			files.add(file4);
		}
		if (file5 != null) {
			files.add(file5);
		}
		return files.toArray(new MultipartFile[files.size()]);
	}
}
```
  1. Create a **CommentController.java** to handle form post.
```
@Controller
public class CommentController {
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String addComment(CommentForm commentForm) throws IOException {

		MultipartFile[] files = commentForm.getFiles();

		for (int i = 0; i < files.length; i++) {
			// Do stuffs!!!
		}

		return "redirect:/saved"; // redirect after processing!!!
	}
}
```