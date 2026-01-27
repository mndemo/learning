<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="penaltyWarningHouseholdFollowup (input, data)" 
          th:with="formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                   inputData=${data.get(input.name)}">
  <div class="spacing-above-25 form-group" th:classappend="${!inputData.valid(data)} ? 'form-group--error' : ''">
    <div class="word-wrap-break-word">
      <fieldset class="checkbox-group">
        <legend class="form-question" th:text="#{penalty-warning.which-household-member-applies}"></legend>
        <p class="text--help" th:text="#{penalty-warning.select-all-that-apply}"></p>
        
        <th:block th:if="${input.options != null and input.options.datasources != null and input.options.datasources.get('personalInfo') != null}"
          th:with="fullName=${input.options.datasources.get('personalInfo').get('firstName').value[0] + ' ' + input.options.datasources.get('personalInfo').get('lastName').value[0]},
                   householdSubFlow=${input.options != null and input.options.subworkflows != null ? input.options.subworkflows.get('household') : null},
                   hasHouseholdMembers=${householdSubFlow != null and not householdSubFlow.isEmpty()}">
          
          <!-- Applicant checkbox -->
          <label th:for="${input.name + '-householdMember-me'}" class="checkbox">
            <input type="checkbox"
              th:id="${input.name + '-householdMember-me'}"
              th:value="${fullName} + ' applicant'"
              th:name="${formInputName}"
              th:checked="${inputData.value.contains(fullName + ' applicant')}">
            <span th:text="|${fullName} #{general.you}|"></span>
          </label>
          
          <!-- Other household member checkboxes (only if they exist) -->
          <th:block th:if="${hasHouseholdMembers}" 
                    th:each="iteration, iterationStat: ${householdSubFlow}">
            <label th:for="|${input.name}-householdMember${iterationStat.index}|"
              class="checkbox"
              th:with="fullName=${iteration.getPagesData().get('householdMemberInfo').get('firstName').value[0] + ' ' + iteration.getPagesData().get('householdMemberInfo').get('lastName').value[0]}">
              <input type="checkbox"
                th:id="|${input.name}-householdMember${iterationStat.index}|"
                th:value="${fullName} + ' ' + ${iteration.id}"
                th:name="${formInputName}"
                th:checked="${inputData.value.contains(fullName + ' ' + iteration.id)}">
              <span th:text="${fullName}"></span>
            </label>
          </th:block>
          
          <!-- If no household members, include hidden checked checkbox for applicant -->
          <th:block th:if="${not hasHouseholdMembers}">
            <input type="checkbox"
              th:name="${formInputName}"
              th:value="${fullName} + ' applicant'"
              checked
              style="display:none;">
          </th:block>
        </th:block>
      </fieldset>
    </div>
    
    <!-- Error message -->
    <div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${input})}"></div>
  </div>
  
  <!-- Hide the follow-up container if there are no household members -->
  <th:block th:if="${input.options != null and input.options.subworkflows != null}">
    <th:block th:with="householdSubFlow=${input.options.subworkflows.get('household')},
                       hasHouseholdMembers=${householdSubFlow != null and not householdSubFlow.isEmpty()}">
      <th:block th:if="${not hasHouseholdMembers}">
        <script th:inline="javascript">
          (function() {
            function onReady(fn) {
              if (document.readyState === "loading") {
                document.addEventListener("DOMContentLoaded", fn);
              } else {
                fn();
              }
            }
            
            onReady(function() {
              const inputName = /*[[${input.name}]]*/ '';
              const followUpContainer = document.querySelector('#' + inputName + '-follow-up');
              if (followUpContainer) {
                followUpContainer.style.display = 'none';
              }
            });
          })();
        </script>
      </th:block>
    </th:block>
  </th:block>
</th:block>
</html>
