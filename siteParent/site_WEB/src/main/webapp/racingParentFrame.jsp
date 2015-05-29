<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import="site.racinggame.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" type="text/css" href="textStyle.css"/>
</head>
<body>
	<jsp:useBean id="identity" class="site.identity.Identity" scope="session"/>
	<jsp:useBean id="racingGame" class="site.racinggame.RacingGame" scope="session"/>
	<jsp:setProperty name="racingGame" property="*"/> 
	<%
		racingGame = RacingGameUtils.getRacingGameObject(identity.getRacingGameIdentifier());
	%>
	<iframe name="moneyFrame" width="100%" height="5%" src="raceMoneyFrame.jsp"></iframe>
	<iframe name="mainRacingFrame" width="100%" height="95%" src="openRacingStore.html?test=2"></iframe>
</body>
</html>