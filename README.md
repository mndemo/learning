  <p class="footer-build-version footer-build-version--desktop"
                   th:if="${shibaBuildVersion != null && !#strings.isEmpty(#strings.trim(shibaBuildVersion))}"
                   th:text="#{generic.footer.build-version(${shibaBuildVersion})}"></p>


                           <p id="footer-build-version" class="footer-build-version footer-build-version--mobile"
           th:if="${shibaBuildVersion != null && !#strings.isEmpty(#strings.trim(shibaBuildVersion))}"
           th:text="#{generic.footer.build-version(${shibaBuildVersion})}"></p>








           .footer-build-version {
  color: #cdcdcd;
  font-size: 0.875rem;
  margin: 0;
}
/* Desktop layout (matches the same breakpoint the footer columns/logo use). */
@media screen and (min-width: 601px) {
  .footer-build-version--desktop {
    margin-top: 0.75rem;
  }
  .footer-build-version--mobile {
    display: none;
  }
}
/* Mobile layout: stack at the bottom; hide the in-About-column copy. */
@media screen and (max-width: 600px) {
  .footer-build-version--desktop {
    display: none;
  }
  .footer-build-version--mobile {
    margin-top: 1.5rem;
  }
}
