What we kept (one small class)
ShibaGlobalModelAttributes.java is still the right place because:
@ControllerAdvice – Spring runs this once for all controllers.
@ModelAttribute("shibaBuildVersion") – Puts shibaBuildVersion on every Thymeleaf model so footer.html can use ${shibaBuildVersion} without changing each controller.
So you don’t need “new concepts” beyond: one global model attribute for the footer.
