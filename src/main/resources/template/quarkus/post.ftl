

    @POST
    @Path("[=path]")
    @Produces("[=produces]")
    @Consumes("[=consumes]")
    <#if jwt=="true">
    @RolesAllowed({ <#list jwtRoles as role>"[=role]" <#sep>, </#sep><#else>"anonymous"</#list>})</#if>
    public [=return] [=methodname]([=params] <#if jwt=="true"> , @Context SecurityContext ctx</#if> )
    {
        log.info("calling Service method [=methodname]");
        [=body]
        return [=val];
    }