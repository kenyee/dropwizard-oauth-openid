<#-- @ftlvariable name="" type="uk.co.froot.demo.openid.views.PublicFreemarkerView" -->
<!DOCTYPE html>
<html lang="en">
<head>
<#include "../includes/common/head.ftl">
</head>

<body>
<div>
<#include "../includes/common/header.ftl">

  <div class="row">
  <h1>Your private data</h1>
  <p>Congratulations! You authenticated through OpenId or OAuth</p>
  <p>This can be seen by administrators and authenticated public</p>
  <p>Try to get to the <a href="/private/admin">admin page</a></p>
  </div>

  <#if model.contacts??>
   <div class="row">
   <h2>Social Contacts</h2>
    <#list model.contacts as c>
	    <div class="col-sm-4 col-md-3">
	      <div class="thumbnail">
	      	  <#if c.profileImageURL??>
	            <a href="${c.profileUrl}"><img src="${c.profileImageURL}"
	             alt="${c.displayName}"></a>
              </#if>
	          <a href="${c.profileUrl}">${c.displayName}</a>
	      </div>
	    </div>
	</#list>
   </div>
  </#if>

  <#if model.albums??>
   <div class="row">
   <h2>Albums</h2>
    <#list model.albums as a>
	    <div class="col-sm-4 col-md-3">
	      <div class="thumbnail">
	      	  <#if a.link??>
	            <a href="${a.link}">
	          </#if>
	          <img src="${a.coverPhoto}" alt="${a.name}">
	      	  <#if a.link??>
	            </a>
	            <a href="${a.link}">
	          </#if>
	          ${a.name}
	      	  <#if a.link??>
	            </a>
	          </#if>
	      </div>
	    </div>
	</#list>
   </div>
  </#if>

<#include "../includes/common/footer.ftl">

</div>

<#include "../includes/common/cdn-scripts.ftl">

</body>
</html>