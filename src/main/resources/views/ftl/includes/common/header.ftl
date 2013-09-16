<#-- @ftlvariable name="" type="uk.co.froot.demo.dojo.views.PublicFreemarkerView" -->
<!-- Add any common HTML here -->
<div class="container">
      <div class="header">
        <ul class="nav nav-pills pull-right">
          <li><a href="/">Home</a></li>
          <#if model.user??>
          <li><a href="/logout">Logout</a></li>
          <#else>
          <li><a href="/login">Login</a></li>
          </#if>
        </ul>
        <h3 class="text-muted">DropWizard OpenID-OAuth Sample</h3>
      </div>
