<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
	<jsp:useBean id="racingGame" class="site.racinggame.RacingGame" scope="session"/>
	<%out.println("identifier: " + racingGame.getRacingIdentifier()); %>
	<%out.println("car ID: " + racingGame.getCarID()); %>
	<a target="rightRacingFrame" href="carDisplayFrame.jsp?option=1"><h4>Car 1</h4></a>
	<a target="rightRacingFrame" href="carDisplayFrame.jsp?option=2"><h4>Car 2</h4></a>
	<a target="rightRacingFrame" href="carDisplayFrame.jsp?option=3"><h4>Car 3</h4></a>
	<a target="rightRacingFrame" href="carDisplayFrame.jsp?option=4"><h4>Car 4</h4></a>
	<a target="rightRacingFrame" href="carDisplayFrame.jsp?option=5"><h4>Car 5</h4></a>
</body>
</html>