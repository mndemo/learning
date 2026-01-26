
<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<!-- Simple Yes/No radio input for penalty warnings - follow-ups are handled via pages-config.yaml -->
<!-- When "Yes" is selected and there are no household members, automatically include applicant -->
<th:block th:fragment="yesNoRadioWithHouseholdFollowup (input, data)" 
          th:with="formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                   inputData=${data.get(input.name)},
                   hasError=${!data.isValid() && !inputData.valid(data)},
                   hasHelpMessage=${input.helpMessageKey != null},
                   isYesSelected=${inputData.value.contains('true')},
                   applicantFullName=${input.options != null and input.options.datasources != null and input.options.datasources.get('personalInfo') != null ? input.options.datasources.get('personalInfo').get('firstName').value[0] + ' ' + input.options.datasources.get('personalInfo').get('lastName').value[0] : ''},
                   hasHouseholdMembers=${input.options != null and input.options.subworkflows != null and input.options.subworkflows.get('household') != null and !input.options.subworkflows.get('household').isEmpty()}">
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
						th:attr="data-applicant-checkbox-id=${input.name + '-applicant-auto'}">
					<span th:text="#{general.inputs.yes}"></span>
				</label>
				
				<label th:for="${#ids.next(input.name)}" class="radio-button"
					style="display:inline-block;margin-right:1.5rem;">
					<input type="radio" 
						th:name="${formInputName}" 
						th:id="${#ids.seq(input.name)}" 
						value="false"
						th:checked="${inputData.value.contains('false')}"
						th:attr="data-applicant-checkbox-id=${input.name + '-applicant-auto'}">
					<span th:text="#{general.inputs.no}"></span>
				</label>
			</div>
			
			<!-- Hidden checkbox for applicant - only checked when "Yes" is selected and NO household members exist -->
			<!-- This ensures validation passes when user lives alone -->
			<th:block th:if="${applicantFullName != null and !applicantFullName.isEmpty() and !hasHouseholdMembers}">
				<input type="checkbox"
					th:id="${input.name + '-applicant-auto'}"
					th:name="${formInputName}"
					th:value="${applicantFullName + ' applicant'}"
					th:checked="${isYesSelected}"
					style="display: none;">
			</th:block>
		</fieldset>
		
		<!-- Error message -->
		<div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${input})}"></div>
	</div>
	
	<!-- JavaScript to automatically check/uncheck applicant checkbox when Yes/No is selected (only when no household members) -->
	<th:block th:if="${!hasHouseholdMembers and applicantFullName != null and !applicantFullName.isEmpty()}">
		<script>
			(function() {
				var applicantCheckboxId = /*[[${input.name + '-applicant-auto'}]]*/ '';
				var yesRadio = document.querySelector('input[type="radio"][value="true"][data-applicant-checkbox-id="' + applicantCheckboxId + '"]');
				var noRadio = document.querySelector('input[type="radio"][value="false"][data-applicant-checkbox-id="' + applicantCheckboxId + '"]');
				var applicantCheckbox = document.getElementById(applicantCheckboxId);
				
				if (yesRadio && applicantCheckbox) {
					yesRadio.addEventListener('change', function() {
						if (this.checked) {
							applicantCheckbox.checked = true;
						}
					});
				}
				
				if (noRadio && applicantCheckbox) {
					noRadio.addEventListener('change', function() {
						if (this.checked) {
							applicantCheckbox.checked = false;
						}
					});
				}
			})();
		</script>
	</th:block>
</th:block>
</html>
