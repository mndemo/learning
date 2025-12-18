<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<!--
  CUSTOM input fragment used ONLY to render shared content once within the form,
  above the yes/no questions (because it's the first input on the page).
-->
<th:block th:fragment="inlineYesNoQuestionLegalAccordion (input, data)">
  <div class="spacing-below-35">
    <div th:replace="~{fragments/legalStuffApplicationTermsAccordion :: legalStuffApplicationTermsAccordion}"></div>
  </div>
</th:block>
</html>

