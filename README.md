  oc set env deployment/mnbenefits-atst-deployment SHIBA_BUILD_VERSION=${{ needs.draft-release.outputs.tag_name }} -n mn-benefits-non-prod
What we kept (one small class)
Exposes shibaBuildVersion to templates(ShibaGlobalModelAttributes.java)
Needed for prod so the running pod gets the tag without you setting the var by hand each time.
ShibaGlobalModelAttributes.java is still the right place because:
@ControllerAdvice – Spring runs this once for all controllers.
@ModelAttribute("shibaBuildVersion") – Puts shibaBuildVersion on every Thymeleaf model so footer.html can use ${shibaBuildVersion} without changing each controller.
So you don’t need “new concepts” beyond: one global model attribute for the footer.
