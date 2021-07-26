
    @Timed(value="[=methodname]")
    @PostMapping(value="[=path]", produces="[=produces]", consumes="[=consumes]")
    <#if jwt=="true">
    @RolesAllowed({ <#list jwtRoles as role>"[=role]" <#sep>, </#sep><#else>"anonymous"</#list>})</#if>
    public [=return] [=methodname](@RequestBody [=params])
    {
        log.info("calling Service method [=methodname]");
        [=body]
        return [=val];
    }