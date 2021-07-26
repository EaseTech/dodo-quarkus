    public [=return] [=methodname]([=params])
    {
    <#if async == true>
        return [=val];
    <#else>
        Optional<[=return]> result = [=val];
        if(result.isEmpty()) {
            return new [=return]();
        } else {
            return result.get();
        }
    </#if>

    }