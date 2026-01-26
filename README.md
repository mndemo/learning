<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<!-- Follow-up fragment for penalty warnings - shows household member checkboxes -->
<th:block th:fragment="penaltyWarningHouseholdFollowup (input, data)"
          th:with="formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                   inputData=${data.get(input.name)}">
  <div class="spacing-above-25 word-wrap-break-word">
    <fieldset class="checkbox-group">
      <legend class="form-question" th:text="#{penalty-warning.which-household-member-applies}"></legend>
      <p class="text--help" th:text="#{penalty-warning.select-all-that-apply}"></p>
      
      <!-- Applicant checkbox (always shown) -->
      <th:block th:if="${input.options != null and input.options.datasources != null and input.options.datasources.get('personalInfo') != null}">
        <label th:for="${input.name + '-householdMember-me'}" class="checkbox"
               th:with="fullName=${input.options.datasources.get('personalInfo').get('firstName').value[0] + ' ' + input.options.datasources.get('personalInfo').get('lastName').value[0]}">
          <input type="checkbox"
                 th:id="${input.name + '-householdMember-me'}"
                 th:value="${fullName + ' applicant'}"
                 th:name="${formInputName}"
                 th:checked="${inputData.value.contains(fullName + ' applicant')}">
          <span th:text="|${fullName} #{general.you}|"></span>
        </label>
      </th:block>
      
      <!-- Other household member checkboxes -->
      <th:block th:if="${input.options != null and input.options.subworkflows != null and input.options.subworkflows.get('household') != null}">
        <th:block th:each="iteration, iterationStat: ${input.options.subworkflows.get('household')}">
          <label th:for="|${input.name}-householdMember${iterationStat.index}|"
                 class="checkbox"
                 th:with="fullName=${iteration.getPagesData().get('householdMemberInfo').get('firstName').value[0] + ' ' + iteration.getPagesData().get('householdMemberInfo').get('lastName').value[0]}">
            <input type="checkbox"
                   th:id="|${input.name}-householdMember${iterationStat.index}|"
                   th:value="${fullName + ' ' + iteration.id}"
                   th:name="${formInputName}"
                   th:checked="${inputData.value.contains(fullName + ' ' + iteration.id)}">
            <span th:text="${fullName}"></span>
          </label>
        </th:block>
      </th:block>
    </fieldset>
  </div>
</th:block>
</html>
