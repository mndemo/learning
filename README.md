<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<!--
  CUSTOM input fragment used ONLY to render the first accordion once within the form,
  above all the yes/no questions (because it's the first input on the page).
  This follows the same pattern as legalStuff page, where accordions are rendered
  via promptMessageFragmentName, but since we need it as the first element, we use
  a custom input fragment instead.
-->
<th:block th:fragment="inlineYesNoQuestionLegalAccordion (input, data)">
  <div class="spacing-below-35">
    <div th:replace="~{fragments/promptMessageFragments/legal-terms-prompt :: legal-stuff-application-terms-accordion('inline-yes-no-question-application-terms-accordion', 'inline-yes-no-question-application-terms-content')}"></div>
  </div>
</th:block>
</html>


