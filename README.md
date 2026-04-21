generic.footer.build-version=Version {0}
    <p id="footer-build-version" class="font-white footer-text text--small spacing-above-25"
           th:if="${shibaBuildVersion != null && !#strings.isEmpty(#strings.trim(shibaBuildVersion))}"
           th:text="#{generic.footer.build-version(${shibaBuildVersion})}"></p>
