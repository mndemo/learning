<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="yesNoRadio (input, data)" 
          th:with="formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                   inputData=${data.get(input.name)},
                   hasError=${!data.isValid() && !inputData.valid(data)},
                   hasHelpMessage=${input.helpMessageKey != null},
                   isYesSelected=${inputData.value.contains('true')}">
  <div class="form-group" th:classappend="${hasError} ? 'form-group--error' : ''">
    <fieldset>
      <div th:replace="~{fragments/form-question-prompt :: formQuestionPrompt(${input})}"></div>
      <p class="text--help" th:if="${hasHelpMessage}" th:text="#{${input.helpMessageKey}}"></p>
      
      <div>
        <label th:for="${#ids.next(input.name)}" class="radio-button"
          style="display:inline-block;margin-right:1.5rem;">
          <input type="radio" 
            th:name="${formInputName}" 
            th:id="${#ids.seq(input.name)}" 
            value="true"
            th:checked="${isYesSelected}"
            th:attrappend="data-follow-up=${input.followUpValues != null and input.followUpValues.contains('true')} ? |#${input.name}-follow-up| : ''">
          <span th:text="#{general.inputs.yes}"></span>
        </label>
        
        <label th:for="${#ids.next(input.name)}" class="radio-button"
          style="display:inline-block;margin-right:1.5rem;">
          <input type="radio" 
            th:name="${formInputName}" 
            th:id="${#ids.seq(input.name)}" 
            value="false"
            th:checked="${inputData.value.contains('false')}">
          <span th:text="#{general.inputs.no}"></span>
        </label>
      </div>
    </fieldset>
    
    <!-- Error message -->
    <div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${input})}"></div>
  </div>
</th:block>
</html>

