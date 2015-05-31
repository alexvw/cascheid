<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import="site.racinggame.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="textStyle.css"/>
<style>
	label{font-size: 24px; font-weight:bold; padding-left: 15px; padding-right: 15px;}
	input{font-size: 24px; font-weight:bold; background-color:lightgrey;width:20%;}
</style>
</head>
<body>
	<jsp:useBean id="racingGame" class="site.racinggame.RacingGame" scope="session"/>
	<script>
		window.open("leftRacingFrame.jsp", "leftFrame");
	</script>
	<div class="inline">
		<label>Money:</label><input value="<%=RacingGameUtils.formatMoney(racingGame.getAvailableCash())%>" readonly>
		<label>Class:</label><input value="<%=racingGame.getRacingClass()%>" readonly>
	</div>
</body>
</html>