<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="penaltyWarningHouseholdFollowup (input, data)" 
          th:with="formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                   inputData=${data.get(input.name)},
                   hasError=${!data.isValid() && !inputData.valid(data)},
                   householdSubFlow=${input.options != null and input.options.subworkflows != null ? input.options.subworkflows.get('household') :null},
                   hasHouseholdMembers=${householdSubFlow != null and !householdSubFlow.isEmpty()}">
  <!-- Only show if household members exist -->
  <div th:if="${hasHouseholdMembers}" class="spacing-above-25 word-wrap-break-word">
    <fieldset class="checkbox-group">
      <legend class="form-question" th:text="#{penalty-warning.which-household-member-applies}"></legend>
      <p class="text--help" th:text="#{penalty-warning.select-all-that-apply}"></p>
      
      <!-- Applicant checkbox -->
      <th:block th:if="${input.options.datasources != null and input.options.datasources.get('personalInfo') != null}">
        <label th:for="${input.name + '-householdMember-me'}" class="checkbox"
          th:with="fullName=${input.options.datasources.get('personalInfo').get('firstName').value[0] + ' ' + input.options.datasources.get('personalInfo').get('lastName').value[0]}">
          <input type="checkbox"
            th:id="${input.name + '-householdMember-me'}"
            th:value="${fullName + ' applicant'}"
            th:name="${formInputName}"
            th:checked="${inputData.value.contains(fullName + ' applicant')}">
          <span th:text="|${fullName} #{general.you}|"></span>
        </label>
        
        <!-- Other household member checkboxes -->
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
    
    <!-- Error message -->
    <div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${input})}"></div>
  </div>
  
  <!-- If no household members, automatically include applicant (follow-up only shows when "true" is selected) -->
  <th:block th:if="${!hasHouseholdMembers and input.options.datasources != null and input.options.datasources.get('personalInfo') != null}"
    th:with="fullName=${input.options.datasources.get('personalInfo').get('firstName').value[0] + ' ' + input.options.datasources.get('personalInfo').get('lastName').value[0]},
             applicantValue=${fullName + ' applicant'}">
    <!-- Hidden checkbox that's always checked (since this follow-up only shows when "true" is selected) -->
    <input type="checkbox"
      th:name="${formInputName}"
      th:value="${applicantValue}"
      checked
      style="display:none;">
  </th:block>
</th:block>
</html>
