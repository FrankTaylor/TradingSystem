<jsp:directive.page contentType="text/html; charset=UTF-8" language="java" />
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jsp:directive.page session="false" />
<c:set var="ctxPath" value="${pageContext.request.contextPath}" />
<c:set var="basePath" value="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}${ctxPath}/" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title>zshop测试</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
  </head>
  
  <body>
    <div>
    	<div>StanWeinstein</font></div>
        <hr />
        <form id="stanWeinsteinFromId" action="${ctxPath}/test/system/StanWeinstein" method="get">
        	<div>
        		StanWeinstein测试：<input type="button" value="执行" onclick="" />
        	</div>
        </form>
        
        <br />
    	<br />
    	
    	<form id="fractalFromId" action="${ctxPath}/system/snap/fractal" method="get">
        	<div>
        		Fractal测试：<input type="button" value="执行" onclick="fractalTest();" />
        	</div>
        </form>
        
    <script type="text/javascript">
    	function stanWeinsteinTest () {
			document.getElementById("stanWeinsteinFromId").submit();
		}
    	
    	function fractalTest () {
    		document.getElementById("fractalFromId").submit();
    	}
    </script>
</body>
</html>
