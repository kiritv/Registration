<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %> 
<%@ taglib prefix="core" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
	<head>
		<title>Update password</title> 
		<style>
		.error
		{
			color: #ff0000;
			font-weight: bold;
		}
		</style>
	</head>
	<body>

		<core:if test="${updatePassword.message != null}">
			
			<h2><core:out value="${updatePassword.message}"/></h2>
			<br>
			
		</core:if>
		<core:if test="${updatePassword.message == null}">
			<h2><spring:message code="lbl.updatepasswprd.page" text="Update Password" /></h2>
			<br/>
			<form:form method="post" modelAttribute="updatePassword" action="/updatePassword/">
				<form:hidden path="token"/>
				<form:hidden path="email"/>
				<table>
					<tr>
						<td><spring:message code="lbl.password" text="Password" /></td>
						<td><form:input path="password" /></td>
						<td><form:errors path="password" cssClass="error" /></td>
					</tr>
					<tr>
						 <td colspan="3">
						 	<input type="submit" value="Update Password"/>
						 </td>
					</tr>
				</table>
			</form:form>
		</core:if>

	</body>
</html>