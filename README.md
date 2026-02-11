<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<!--
  Fragment: householdOptionsCheckboxesWithFollowup (input, data)

  Responsible for:
    - Rendering a checkbox list for the applicant ("you") and each household member.
    - Rendering one or more follow-up questions (amount, frequency, start/end date, etc.)
      for each selected person.

  Data conventions:
    - Main checkbox input: data.get(input.name).value is a list of selected people.
    - For each follow-up (followUp in input.followUps):
        * data.get(followUp.name).value is a flat list of strings used for
          applicant + household members, where:
            - Index 0 (or 0–2 for DATE) belongs to the applicant.
            - Index iterationStat.count (1-based) belongs to that household member
              for MONEY / SELECT.
            - For DATE, each person uses 3 slots (month/day/year) in sequence.
-->
<th:block th:fragment="householdOptionsCheckboxesWithFollowup (input, data)" th:with="
                    personalInfoPage=${(input.options != null and input.options.datasources != null and input.options.datasources.get('personalInfo') != null) ? input.options.datasources.get('personalInfo') : (applicationData != null and applicationData.pagesData != null and applicationData.pagesData.get('personalInfo') != null) ? applicationData.pagesData.get('personalInfo') : (pageDatasources != null and pageDatasources.get('personalInfo') != null) ? pageDatasources.get('personalInfo') : null},
                    yourFullName=${personalInfoPage != null and personalInfoPage.get('firstName') != null and !personalInfoPage.get('firstName').value.isEmpty() and personalInfoPage.get('lastName') != null and !personalInfoPage.get('lastName').value.isEmpty() ? (personalInfoPage.get('firstName').value[0] + ' ' + personalInfoPage.get('lastName').value[0]) : 'You'},
                    formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                    inputData=${data.get(input.name)},
          			inputFollowupsName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.followUps[0].name)},
                    inputDataNames=${data.get(input.name)},
					sortedHouseholdMembers=${T(org.codeforamerica.shiba.pages.PageUtils).householdMemberSort(inputDataNames.value)},
                    inputFollowupData=${data.get(input.followUps[0].name)},
                    hasError=${applicationData != null ? (!data.isValid(applicationData) && !inputData.valid(data, applicationData)) : (!data.isValid() && !inputData.valid(data))},
                    hasFollowUpError=${applicationData != null ? (!data.isValid(applicationData) && inputFollowupData != null && !inputFollowupData.valid(data, applicationData)) : (!data.isValid() && inputFollowupData != null && !inputFollowupData.valid(data))},
                    mainInputErrorKeys=${applicationData != null ? inputData.errorMessageKeys(data, applicationData) : inputData.errorMessageKeys(data)},
                    followUpErrorKeys=${inputFollowupData != null ? (applicationData != null ? inputFollowupData.errorMessageKeys(data, applicationData) : inputFollowupData.errorMessageKeys(data)) : T(java.util.Collections).emptyList()},
                    inputFollowupsHasErrors=${hasFollowUpError},
                    hasHelpMessage=${input.helpMessageKey != null},
                    needsAriaLabel=${input.needsAriaLabel()},
                    youIsChecked=${T(org.codeforamerica.shiba.pages.PageUtils).listOfNamesContainsName(sortedHouseholdMembers, yourFullName + ' applicant')},
                    noPersonsChecked=${#arrays.isEmpty(sortedHouseholdMembers)}
                    ">
	<div class="form-group" th:classappend="${hasError} ? 'form-group--error' : ''">
		<!--/* form-group div is to display the orange error line when no persons are selected. */-->
		<div class="question-with-follow-up" style="margin-bottom: 1rem;">
			<div class="question-with-follow-up__question">
				<div class="form-group">
					<label th:for="householdMember-me" class="checkbox display-flex" style="margin-bottom: 1rem;">
						<input type="checkbox" th:id="householdMember-me" th:value="${yourFullName} + ' applicant'"
							th:name="${formInputName}" th:checked="${youIsChecked}" 
							th:attrappend="data-follow-up=|#${input.name}-follow-up|">
						<span th:text="|${yourFullName} #{general.you}|"> </span>
					</label>
				</div>
			</div>
			<div class="question-with-follow-up__follow-up" th:id="|${input.name}-follow-up|">
				<!--
				  APPLICANT FOLLOW-UPS: For each follow-up (amount, frequency, start date, end date, etc.)
				  we need this follow-up's saved data and its form field name.
				-->
				<!--
				  FOLLOW-UP EXISTENCE: We do not check "if follow-ups exist". We loop over input.followUps
				  (defined in pages-config.yaml for this input). If the list is empty, the loop runs 0 times
				  and no follow-up UI is rendered. If it has amount, frequency, start date, end date, we render
				  one block per follow-up. Whether we show DATE/SELECT/MONEY is decided by th:switch on
				  youFollowUp.type (each follow-up has a type in config: MONEY, SELECT, DATE, etc.).
				-->
				<!--
				  currentFollowupData = saved answers for THIS follow-up only (e.g. veteransBenefitsAmount).
				    For the applicant we only use value[0] (or value[0], value[1], value[2] for a DATE).
				  currentFollowupFormName = HTML form field name for this follow-up (e.g. veteransBenefitsAmount).
				    Used as the 'name' attribute so the server receives this follow-up's value under this key.
				-->
				<th:block th:each="youFollowUp, followUpStat : ${input.followUps}"
					th:with="currentFollowupData=${data.get(youFollowUp.name)},
					         currentFollowupFormName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(youFollowUp.name)}">
					<div th:replace="~{fragments/form-question-prompt :: formQuestionPrompt(${youFollowUp})}"></div>
					<p class="text--help" th:id="${youFollowUp.name + '-help-message'}" th:if="${youFollowUp.helpMessageKey != null}"
						th:utext="#{${youFollowUp.helpMessageKey}}"></p>
					<!--
					  Switch on follow-up type so we render the right control: MONEY, SELECT, DATE, or CHECKBOX (e.g. No end date).
					-->
					<th:block th:switch="${youFollowUp.type}">
						<div class="form-group" th:classappend="${inputFollowupsHasErrors} ? 'form-group--error' : ''">
							<!-- MONEY case: dollar amount input for the applicant -->
							<div class="text-input-group" 
								th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).MONEY}">
								<div class="text-input-group__prefix" style="background-color:#FFFFFF">$</div>
								<!--
								  Applicant amount input:
								  - name = currentFollowupFormName (e.g. veteransBenefitsAmount) so it posts under that key.
								  - value = currentFollowupData.value[0] because the applicant is always at index 0.
								-->
								<input type="text" class="text-input" th:attr="aria-describedby=${youFollowUp.helpMessageKey != null ? youFollowUp.name + '-help-message' : ''},
			                            aria-labelledby=${needsAriaLabel ? youFollowUp.name+'-label' : ''},
			                            aria-invalid=${hasError}" th:id="|${youFollowUp.name}|" th:name="${currentFollowupFormName}"
									th:value="${(!currentFollowupData.value.isEmpty()) ? currentFollowupData.value[0] : ''}">
								<div class="text-input-group__postfix" style="white-space: nowrap; background-color:#FFFFFF" 
									th:if="${youFollowUp.inputPostfix != null}" th:text="#{${youFollowUp.inputPostfix}}"></div>
							</div>
						</div>
						<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).SELECT}" class="form-group">
							<div class="select">
								<select th:id="${youFollowUp.name}" class="select__element"
									th:name="${currentFollowupFormName}"
									th:attr="aria-invalid=${hasError}">
									<th:block th:each="option: ${youFollowUp.options.selectableOptions}">
										<option th:value="${option.value}" th:text="#{${option.messageKey}}"
											th:selected="${!currentFollowupData.value.isEmpty() and currentFollowupData.value[0] == option.value}"></option>
									</th:block>
								</select>
							</div>
						</div>
						<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).DATE}" class="form-group"
							th:with="isEndDate=${#strings.endsWith(youFollowUp.name, 'EndDate')},
								noEndDateDataApplicant=${#strings.endsWith(youFollowUp.name, 'EndDate') ? data.get(#strings.substring(youFollowUp.name, 0, #strings.length(youFollowUp.name) - 8) + 'NoEndDate') : null},
								noEndDateChecked=${isEndDate and noEndDateDataApplicant != null and !noEndDateDataApplicant.value.isEmpty() and noEndDateDataApplicant.value[0] == 'true'}">
							<fieldset class="date-input">
								<p class="text--help">
									<label th:for="${youFollowUp.name}+'-month'" th:text="#{general.month}"></label>
									&nbsp;/&nbsp;
									<label th:for="${youFollowUp.name}+'-day'" th:text="#{general.day}"></label>
									&nbsp;/&nbsp;
									<label th:for="${youFollowUp.name}+'-year'" th:text="#{general.year}"></label>
								</p>
								<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
									th:id="${youFollowUp.name}+'-month'" th:name="${currentFollowupFormName}"
									th:value="${(#lists.size(currentFollowupData.value) > 0) ? currentFollowupData.value[0] : ''}"
									th:placeholder="mm" th:disabled="${isEndDate and noEndDateChecked}"/>
								&nbsp;/&nbsp;
								<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
									th:id="${youFollowUp.name}+'-day'" th:name="${currentFollowupFormName}"
									th:value="${(#lists.size(currentFollowupData.value) > 1) ? currentFollowupData.value[1] : ''}"
									th:placeholder="dd" th:disabled="${isEndDate and noEndDateChecked}"/>
								&nbsp;/&nbsp;
								<input type="text" inputmode="numeric" maxlength="4" class="text-input form-width--4-character dob-input"
									th:id="${youFollowUp.name}+'-year'" th:name="${currentFollowupFormName}"
									th:value="${(#lists.size(currentFollowupData.value) > 2) ? currentFollowupData.value[2] : ''}"
									th:placeholder="yyyy" th:disabled="${isEndDate and noEndDateChecked}"/>
							</fieldset>
							<!--
							  NO END DATE (applicant, multi end-date safe):
							  When this follow-up is an End Date, we wire its 3 inputs to the "No end date"
							  checkbox that immediately follows (next form-group). We scope by this form-group
							  and by "next sibling form-group" so each end date on the page only affects its own
							  inputs and checkbox. Reusable: same pattern for any DATE+CHECKBOX pair in order.
							-->
							<th:block th:if="${isEndDate}">
								<script>
									(function() {
										// Get the script element so we can find the form-group that contains THIS end date block only
										var scriptEl = document.currentScript;
										// Find the nearest parent with class "form-group" (the container for this end date's 3 inputs)
										var formGroup = scriptEl && scriptEl.closest && scriptEl.closest('.form-group');
										if (!formGroup) return;
										// Get only the 3 date inputs (month/day/year) inside THIS form-group
										// So multiple end dates on the page each control only their own inputs
										var inputs = formGroup.querySelectorAll('input.dob-input');
										// The "No end date" checkbox is always the next form-group (follow-up order: DATE then CHECKBOX)
										var next = formGroup.nextElementSibling;
										// Skip any non-form-group siblings (e.g. whitespace nodes) until we find the next form-group
										while (next && (!next.classList || !next.classList.contains('form-group'))) next = next.nextElementSibling;
										// The checkbox is the single checkbox inside that next form-group
										var checkbox = next ? next.querySelector('input[type="checkbox"]') : null;
										// Sync disabled state of the 3 inputs to the checkbox; when disabled, clear values
										function sync() {
											if (checkbox && inputs.length === 3) {
												inputs.forEach(function(inp) {
													inp.disabled = checkbox.checked;
													if (checkbox.checked) inp.value = '';
												});
											}
										}
										if (checkbox) { sync(); checkbox.addEventListener('change', sync); }
										if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', sync);
									})();
								</script>
							</th:block>
						</div>
						<!-- Single checkbox follow-up (e.g. "No end date") - when checked, disables the preceding End Date inputs (script in DATE case). -->
						<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).CHECKBOX}" class="form-group"
							th:if="${youFollowUp.options != null and youFollowUp.options.selectableOptions != null and #lists.size(youFollowUp.options.selectableOptions) == 1}">
							<th:block th:with="singleOption=${youFollowUp.options.selectableOptions[0]}">
								<label th:for="${youFollowUp.name}" class="checkbox display-flex">
									<input type="checkbox" th:id="${youFollowUp.name}" th:name="${currentFollowupFormName}"
										th:value="${singleOption.value}"
										th:checked="${!currentFollowupData.value.isEmpty() and currentFollowupData.value[0] == singleOption.value}">
									<span th:text="#{${singleOption.messageKey}}"></span>
								</label>
							</th:block>
						</div>
					</th:block>
				</th:block>
			</div>
		</div>
		<!--
		  Household members (empty when applicant-only).
		    - Iterate over the 'household' subworkflow; use empty list when null so applicant-only case works.
		    - Each iteration represents one household member with their own checkbox and follow-ups.
		-->
		<th:block th:each="iteration, iterationStat: ${input.options.subworkflows != null and input.options.subworkflows.get('household') != null ? input.options.subworkflows.get('household') : T(java.util.Collections).emptyList()}"
			th:with="fullName=${iteration.getPagesData().get('householdMemberInfo').get('firstName').value[0]} + ' ' + ${iteration.getPagesData().get('householdMemberInfo').get('lastName').value[0]}">
			<div class="question-with-follow-up" style="margin-bottom: 1rem;">
				<div class="question-with-follow-up__question">
					<div class="form-group">

						<label th:for="|${formInputName}${iterationStat.index}|" class="checkbox display-flex">
							<input type="checkbox" th:id="|${formInputName}${iterationStat.index}|"
								th:value="${fullName} + ' ' + ${iteration.id}" th:name="${formInputName}"
								th:checked="${T(org.codeforamerica.shiba.pages.PageUtils).listOfNamesContainsName(sortedHouseholdMembers, fullName + ' ' + iteration.id)}"
								th:attrappend="data-follow-up=|#${input.name}${iterationStat.index}-follow-up|">
							<span th:text="${fullName}"></span>
						</label>
					</div>
				</div> 
				
				<!-- Follow-up container for this specific household member -->
				<div class="question-with-follow-up__follow-up" th:id="|${input.name}${iterationStat.index}-follow-up|">
					<th:block th:each="followUp, followUpStatMember : ${input.followUps}"
						th:with="currentFollowupData=${data.get(followUp.name)},
						         currentFollowupFormName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(followUp.name)}">
						<div class="form-group" th:classappend="${inputFollowupsHasErrors} ? 'form-group--error' : ''">
							<div th:replace="~{fragments/form-question-prompt :: formQuestionPrompt(${followUp})}"></div>
							<p class="text--help" th:id="|${followUp.name}${iterationStat.index}-help-message|" th:if="${followUp.helpMessageKey != null}"
								th:utext="#{${followUp.helpMessageKey}}"></p>
							<!--
							  Render this household member's value for each follow-up type.
							  Indexing rules:
							    - MONEY / SELECT: use currentFollowupData.value[iterationStat.count]
							      (0 is applicant, 1 is first household member, etc.).
							    - DATE: use 3 entries per person in sequence:
							        applicant: indices 0,1,2
							        member with count N: indices N*3, N*3+1, N*3+2
							-->
							<th:block th:switch="${followUp.type}">
								<!-- Including switch/case for future addition of other input types. -->
								<div class="text-input-group" 
									th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).MONEY}">
									<div class="text-input-group__prefix" style="background-color: #FFFFFF">$</div>
									<input type="text" class="text-input" th:attr="aria-describedby=${followUp.helpMessageKey != null ? followUp.name + iterationStat.index + '-help-message' : ''},
                                        aria-labelledby=${needsAriaLabel ? followUp.name+'-label' : ''},
                                        aria-invalid=${hasError}" th:id="|${followUp.name}${iterationStat.index}|"
										th:name="${currentFollowupFormName}"
										th:value="${(iterationStat.count < currentFollowupData.value.size() && !currentFollowupData.value[iterationStat.count].isEmpty()) ? currentFollowupData.value[iterationStat.count] : ''}">
										<div class="text-input-group__postfix" style="white-space: nowrap;background-color: #FFFFFF" 
											th:if="${followUp.inputPostfix != null}" th:text="#{${followUp.inputPostfix}}"></div>
								</div>
								<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).SELECT}" class="form-group">
									<div class="select">
										<select th:id="|${followUp.name}${iterationStat.index}|" class="select__element"
											th:name="${currentFollowupFormName}"
											th:attr="aria-invalid=${hasError}">
											<th:block th:each="option: ${followUp.options.selectableOptions}">
												<option th:value="${option.value}" th:text="#{${option.messageKey}}"
													th:selected="${iterationStat.count < currentFollowupData.value.size() && currentFollowupData.value[iterationStat.count] == option.value}"></option>
											</th:block>
										</select>
									</div>
								</div>
								<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).DATE}" class="form-group"
									th:with="isEndDateMember=${#strings.endsWith(followUp.name, 'EndDate')},
										noEndDateDataMember=${#strings.endsWith(followUp.name, 'EndDate') ? data.get(#strings.substring(followUp.name, 0, #strings.length(followUp.name) - 8) + 'NoEndDate') : null},
										noEndDateCheckedMember=${isEndDateMember and noEndDateDataMember != null and iterationStat.count < noEndDateDataMember.value.size() and noEndDateDataMember.value[iterationStat.count] == 'true'}">
									<fieldset class="date-input">
										<p class="text--help">
											<label th:for="|${followUp.name}${iterationStat.index}-month|" th:text="#{general.month}"></label>
											&nbsp;/&nbsp;
											<label th:for="|${followUp.name}${iterationStat.index}-day|" th:text="#{general.day}"></label>
											&nbsp;/&nbsp;
											<label th:for="|${followUp.name}${iterationStat.index}-year|" th:text="#{general.year}"></label>
										</p>
										<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
											th:id="|${followUp.name}${iterationStat.index}-month|" th:name="${currentFollowupFormName}"
											th:value="${(iterationStat.count*3 + 0 < currentFollowupData.value.size()) ? currentFollowupData.value[iterationStat.count*3] : ''}"
											th:placeholder="mm" th:disabled="${isEndDateMember and noEndDateCheckedMember}"/>
										&nbsp;/&nbsp;
										<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
											th:id="|${followUp.name}${iterationStat.index}-day|" th:name="${currentFollowupFormName}"
											th:value="${(iterationStat.count*3 + 1 < currentFollowupData.value.size()) ? currentFollowupData.value[iterationStat.count*3 + 1] : ''}"
											th:placeholder="dd" th:disabled="${isEndDateMember and noEndDateCheckedMember}"/>
										&nbsp;/&nbsp;
										<input type="text" inputmode="numeric" maxlength="4" class="text-input form-width--4-character dob-input"
											th:id="|${followUp.name}${iterationStat.index}-year|" th:name="${currentFollowupFormName}"
											th:value="${(iterationStat.count*3 + 2 < currentFollowupData.value.size()) ? currentFollowupData.value[iterationStat.count*3 + 2] : ''}"
											th:placeholder="yyyy" th:disabled="${isEndDateMember and noEndDateCheckedMember}"/>
									</fieldset>
									<!--
									  NO END DATE (household member, multi end-date safe):
									  Same pattern as applicant: wire this member's end date inputs to the
									  "No end date" checkbox in the next form-group. Scoped by form-group
									  and next sibling so multiple members and multiple income types work.
									-->
									<th:block th:if="${isEndDateMember}">
										<script>
											(function() {
												// Get the script element so we can find the form-group for THIS member's end date only
												var scriptEl = document.currentScript;
												// Find the nearest parent with class "form-group" (the container for this end date's 3 inputs)
												var formGroup = scriptEl && scriptEl.closest && scriptEl.closest('.form-group');
												if (!formGroup) return;
												// Get only the 3 date inputs (month/day/year) inside THIS form-group
												// So multiple members and multiple income types each control only their own inputs
												var inputs = formGroup.querySelectorAll('input.dob-input');
												// The "No end date" checkbox for this member is the next form-group (order: DATE then CHECKBOX)
												var next = formGroup.nextElementSibling;
												// Skip any non-form-group siblings until we find the next form-group
												while (next && (!next.classList || !next.classList.contains('form-group'))) next = next.nextElementSibling;
												// The checkbox is the single checkbox inside that next form-group
												var checkbox = next ? next.querySelector('input[type="checkbox"]') : null;
												// Sync disabled state of the 3 inputs to the checkbox; when disabled, clear values
												function sync() {
													if (checkbox && inputs.length === 3) {
														inputs.forEach(function(inp) {
															inp.disabled = checkbox.checked;
															if (checkbox.checked) inp.value = '';
														});
													}
												}
												if (checkbox) { sync(); checkbox.addEventListener('change', sync); }
												if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', sync);
											})();
										</script>
									</th:block>
								</div>
								<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).CHECKBOX}" class="form-group"
									th:if="${followUp.options != null and followUp.options.selectableOptions != null and #lists.size(followUp.options.selectableOptions) == 1}">
									<th:block th:with="singleOpt=${followUp.options.selectableOptions[0]}">
										<label th:for="|${followUp.name}${iterationStat.index}|" class="checkbox display-flex">
											<input type="checkbox" th:id="|${followUp.name}${iterationStat.index}|" th:name="${currentFollowupFormName}"
												th:value="${singleOpt.value}"
												th:checked="${iterationStat.count < currentFollowupData.value.size() and currentFollowupData.value[iterationStat.count] == singleOpt.value}">
											<span th:text="#{${singleOpt.messageKey}}"></span>
										</label>
									</th:block>
								</div>
							</th:block>
						</div>
					</th:block>
				</div>
			</div>
		</th:block>
		<!-- Main checkbox error: show only when this input actually failed validation -->
		<div th:if="${hasError && input.validationIcon && !#lists.isEmpty(mainInputErrorKeys)}">
			<p class="text--error" th:aria-label="#{error.title}" th:id="${input.name + '-error-p'}">
				<th:block th:each="errorKey, iter : ${mainInputErrorKeys}">
					<i th:if="${input.validationIcon}" class="icon-warning" th:id="${input.name + '-error-icon-' + (iter.index + 1)}"></i>
					<span th:id="${input.name + '-error-message-' + (iter.index + 1)}" th:class="${input.name + '-error'}" th:text="#{${errorKey}}"></span>
					<br th:if="${!iter.last}"/>
				</th:block>
			</p>
		</div>
		<!-- Follow-up error: show only when follow-up input actually failed validation and at least one person is selected -->
		<div th:if="${hasFollowUpError && !noPersonsChecked && input.followUps[0].validationIcon && !#lists.isEmpty(followUpErrorKeys)}" th:with="followUpInput=${input.followUps[0]}">
			<p class="text--error" th:aria-label="#{error.title}" th:id="${followUpInput.name + '-error-p'}">
				<th:block th:each="errorKey, iter : ${followUpErrorKeys}">
					<i th:if="${followUpInput.validationIcon}" class="icon-warning" th:id="${followUpInput.name + '-error-icon-' + (iter.index + 1)}"></i>
					<span th:id="${followUpInput.name + '-error-message-' + (iter.index + 1)}" th:class="${followUpInput.name + '-error'}" th:text="#{${errorKey}}"></span>
					<br th:if="${!iter.last}"/>
				</th:block>
			</p>
		</div>
	</div>
</th:block>
</html>
