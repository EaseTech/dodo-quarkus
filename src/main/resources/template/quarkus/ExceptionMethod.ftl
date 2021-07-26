
    @ExceptionHandler([=entityType]NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handle[=entityType]NotFoundException(
        [=entityType]NotFoundException ex) {

        return handleException(ex);
    }