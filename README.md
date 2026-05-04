  <p id="footer-build-version" class="footer-build-version"
           th:if="${shibaBuildVersion != null && !#strings.isEmpty(#strings.trim(shibaBuildVersion))}"
           th:text="#{generic.footer.build-version(${shibaBuildVersion})}"></p>




           .footer-build-version {
  clear: both;
  color: #cdcdcd;
  font-size: 0.875rem;
  margin: 1.5rem 0 0;
}
@media screen and (min-width: 601px) {
  .footer-build-version {
    text-align: right;
  }
}
