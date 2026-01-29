<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<th:block th:fragment="yesNoRadioWithHouseholdFollowup (input, data)" 
          th:with="formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                   inputData=${data.get(input.name)},
                   hasError=${!data.isValid() && !inputData.valid(data)},
                   hasHelpMessage=${input.helpMessageKey != null},
                   isYesSelected=${inputData.value.contains('true')},
				   householdSubFlow=${input.options != null and input.options.subworkflows != null ? input.options.subworkflows.get('household') : null},
				   hasHouseholdMembers=${householdSubFlow != null and !householdSubFlow.isEmpty()}"> 		   
	<div class="form-group" th:classappend="${hasError } ? 'form-group--error' : ''">
		<!-- Question with Follow-up wrapper/Honeycrisp container that enables the show/hide behavior for follow-ups. When no household members, data attributes are used by script to add hidden "applicant" only when Yes is selected. -->
		<div class="question-with-follow-up"
			th:attr="data-no-household-members=${!hasHouseholdMembers ? 'true' : null},
			         data-applicant-value=${(!hasHouseholdMembers && input.options.datasources != null) ? (input.options.datasources.get('personalInfo').get('firstName').value[0] + ' ' + input.options.datasources.get('personalInfo').get('lastName').value[0] + ' applicant') : null},
			         data-input-name=${!hasHouseholdMembers ? formInputName : null}">
			<!-- Main Yes/No Question -->
			<div class="question-with-follow-up__question">
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
								th:attr="data-follow-up=${hasHouseholdMembers ? '#' + input.name + '-household-followup' : null}">
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
			</div>
			
			<!-- When there are NO household members, a script (below) adds a hidden "applicant" input only when the user selects Yes, so NOT_BLANK still requires an answer and SELECT_AT_LEAST_ONE_IF_YES_SELECTED passes. -->

			<!-- Follow-up: Household Member Checkboxes (ONLY shows if there are household members). Validation is server-side via SELECT_AT_LEAST_ONE_IF_YES_SELECTED. -->
			<div class="question-with-follow-up__follow-up" 
				th:id="${input.name + '-household-followup'}"
				th:if="${hasHouseholdMembers}">
				
				<div class="spacing-above-25 word-wrap-break-word">
					<fieldset class="checkbox-group" th:classappend="${hasError} ? 'form-group--error' : ''">
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
				</div>
			</div>
		</div>
		
		<!-- Error message -->
		<div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${input})}"></div>
	</div>

	<!-- When no household members: add hidden "applicant" only when Yes is selected so NOT_BLANK still enforces answering and SELECT_AT_LEAST_ONE_IF_YES_SELECTED passes. -->
	<script th:if="${!hasHouseholdMembers}">
		(function() {
			function run() {
				document.querySelectorAll('.question-with-follow-up[data-no-household-members="true"]').forEach(function(wrapper) {
					var name = wrapper.getAttribute('data-input-name');
					var applicantValue = wrapper.getAttribute('data-applicant-value');
					if (!name || !applicantValue) return;
					var radios = wrapper.querySelectorAll('input[type="radio"]');
					var nameMatches = function(el) { return el.getAttribute('name') === name; };
					var radioWithName = Array.prototype.find.call(radios, function(r) { return nameMatches(r); });
					if (!radioWithName) return;
					var checked = Array.prototype.find.call(radios, function(r) { return nameMatches(r) && r.checked; });
					var isYes = checked && checked.value === 'true';
					var hidden = wrapper.querySelector('input[type="hidden"][data-penalty-applicant]');
					if (isYes) {
						if (!hidden) {
							var input = document.createElement('input');
							input.type = 'hidden';
							input.name = name;
							input.value = applicantValue;
							input.setAttribute('data-penalty-applicant', 'true');
							wrapper.appendChild(input);
						}
					} else {
						if (hidden) hidden.remove();
					}
				});
			}
			if (document.readyState === 'loading') {
				document.addEventListener('DOMContentLoaded', run);
			} else {
				run();
			}
			document.addEventListener('change', function(e) {
				if (e.target.type === 'radio' && e.target.form) run();
			});
		})();
	</script>
 </th:block>
</html>
