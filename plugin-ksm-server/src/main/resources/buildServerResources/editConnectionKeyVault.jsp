<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/include-internal.jsp" %>

<jsp:useBean id="project" type="jetbrains.buildServer.serverSide.SProject" scope="request"/>
<jsp:useBean id="oauthConnectionBean" type="jetbrains.buildServer.serverSide.oauth.OAuthConnectionBean" scope="request"/>
<jsp:useBean id="propertiesBean" type="jetbrains.buildServer.serverSide.oauth.OAuthConnectionBean" scope="request"/>
<jsp:useBean id="keys" class="com.keepersecurity.teamcity.secretsmanager.common.KsmTokenJspKeys"/>

<tr>
  <td><label for="displayName">Display name:</label><l:star/></td>
  <td>
    <props:textProperty name="displayName" className="longField"/>
    <span class="smallNote">Use a name to distinguish this connection from others.</span>
    <span class="error" id="error_displayName"></span>
  </td>
</tr>

<tr class="noBorder">
  <td><label for="${keys.CLIENT_SECRET}">Client secret:</label></td>
  <td>
    <props:passwordProperty name="${keys.CLIENT_SECRET}" className="longField textProperty_max-width js_max-width"/>
    <span class="error" id="error_${keys.CLIENT_SECRET}"/>
    <span class="smallNote">KSM config secret.</span>
  </td>
</tr>
