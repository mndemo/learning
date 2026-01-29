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
<th:block th:fragment="householdOptionsCheckboxesWithFollowup (input, data)" th:with="formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
          			inputData=${data.get(input.name)},
          			inputFollowupsName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.followUps[0].name)},
          			inputDataErrors=${input.validationErrorMessageKeys},
                    inputDataNames=${data.get(input.name)},
					sortedHouseholdMembers=${T(org.codeforamerica.shiba.pages.PageUtils).householdMemberSort(inputDataNames.value)},
                    inputFollowupData=${data.get(input.followUps[0].name)},
                    inputFollowupDataErrors=${input.followUps[0].validationErrorMessageKeys},
                    inputFollowupsHasErrors=!${#arrays.isEmpty(inputFollowupDataErrors)},
                    hasError=${applicationData != null ? (!data.isValid(applicationData) && !inputData.valid(data, applicationData)) : (!data.isValid() && !inputData.valid(data))},
                    hasHelpMessage=${input.helpMessageKey != null},
                    yourFullName=${input.options.datasources.get('personalInfo').get('firstName').value[0] + ' ' + input.options.datasources.get('personalInfo').get('lastName').value[0]},
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
					         currentFollowupFormName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(youFollowUp.name)},
					         nextFollowUp=${(followUpStat.index + 1 < #lists.size(input.followUps)) ? input.followUps[followUpStat.index + 1] : null},
					         isEndDateWithNoEndDate=${nextFollowUp != null and #strings.endsWith(nextFollowUp.name, 'NoEndDate')},
					         noEndDateChecked=${isEndDateWithNoEndDate and data.get(nextFollowUp.name).value.size() > 0 and data.get(nextFollowUp.name).value[0] == 'true'}">
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
						<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).DATE}" class="form-group">
							<fieldset class="date-input" th:attr="data-end-date-fieldset=${isEndDateWithNoEndDate ? youFollowUp.name : null}">
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
									th:disabled="${noEndDateChecked}" th:placeholder="mm"/>
								&nbsp;/&nbsp;
								<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
									th:id="${youFollowUp.name}+'-day'" th:name="${currentFollowupFormName}"
									th:value="${(#lists.size(currentFollowupData.value) > 1) ? currentFollowupData.value[1] : ''}"
									th:disabled="${noEndDateChecked}" th:placeholder="dd"/>
								&nbsp;/&nbsp;
								<input type="text" inputmode="numeric" maxlength="4" class="text-input form-width--4-character dob-input"
									th:id="${youFollowUp.name}+'-year'" th:name="${currentFollowupFormName}"
									th:value="${(#lists.size(currentFollowupData.value) > 2) ? currentFollowupData.value[2] : ''}"
									th:disabled="${noEndDateChecked}" th:placeholder="yyyy"/>
							</fieldset>
						</div>
						<!-- Single checkbox follow-up (e.g. "No end date") - when checked, disables the previous DATE via script. -->
						<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).CHECKBOX}" class="form-group"
							th:if="${youFollowUp.options != null and youFollowUp.options.selectableOptions != null and #lists.size(youFollowUp.options.selectableOptions) == 1}"
							th:attr="data-no-end-date-checkbox=${#strings.endsWith(youFollowUp.name, 'NoEndDate') ? input.followUps[followUpStat.index - 1].name : null}">
							<th:block th:with="singleOption=${youFollowUp.options.selectableOptions[0]}">
								<label th:for="${youFollowUp.name}" class="checkbox display-flex">
									<input type="checkbox" th:id="${youFollowUp.name}" th:name="${currentFollowupFormName}"
										th:value="${singleOption.value}"
										th:checked="${!currentFollowupData.value.isEmpty() and currentFollowupData.value[0] == singleOption.value}"
										class="no-end-date-checkbox">
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
						         currentFollowupFormName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(followUp.name)},
						         nextFollowUpMember=${(followUpStatMember.index + 1 < #lists.size(input.followUps)) ? input.followUps[followUpStatMember.index + 1] : null},
						         isEndDateWithNoEndDateMember=${nextFollowUpMember != null and #strings.endsWith(nextFollowUpMember.name, 'NoEndDate')},
						         noEndDateCheckedMember=${isEndDateWithNoEndDateMember and data.get(nextFollowUpMember.name).value.size() > iterationStat.count and data.get(nextFollowUpMember.name).value[iterationStat.count] == 'true'}">
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
								<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).DATE}" class="form-group">
									<fieldset class="date-input" th:attr="data-end-date-fieldset=${isEndDateWithNoEndDateMember ? followUp.name : null}, data-end-date-member-index=${isEndDateWithNoEndDateMember ? iterationStat.index : null}">
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
											th:disabled="${noEndDateCheckedMember}" th:placeholder="mm"/>
										&nbsp;/&nbsp;
										<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
											th:id="|${followUp.name}${iterationStat.index}-day|" th:name="${currentFollowupFormName}"
											th:value="${(iterationStat.count*3 + 1 < currentFollowupData.value.size()) ? currentFollowupData.value[iterationStat.count*3 + 1] : ''}"
											th:disabled="${noEndDateCheckedMember}" th:placeholder="dd"/>
										&nbsp;/&nbsp;
										<input type="text" inputmode="numeric" maxlength="4" class="text-input form-width--4-character dob-input"
											th:id="|${followUp.name}${iterationStat.index}-year|" th:name="${currentFollowupFormName}"
											th:value="${(iterationStat.count*3 + 2 < currentFollowupData.value.size()) ? currentFollowupData.value[iterationStat.count*3 + 2] : ''}"
											th:disabled="${noEndDateCheckedMember}" th:placeholder="yyyy"/>
									</fieldset>
								</div>
								<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).CHECKBOX}" class="form-group"
									th:if="${followUp.options != null and followUp.options.selectableOptions != null and #lists.size(followUp.options.selectableOptions) == 1}">
									<th:block th:with="singleOpt=${followUp.options.selectableOptions[0]}">
										<label th:for="|${followUp.name}${iterationStat.index}|" class="checkbox display-flex">
											<input type="checkbox" th:id="|${followUp.name}${iterationStat.index}|" th:name="${currentFollowupFormName}"
												th:value="${singleOpt.value}"
												th:checked="${iterationStat.count < currentFollowupData.value.size() and currentFollowupData.value[iterationStat.count] == singleOpt.value}"
												class="no-end-date-checkbox" th:attr="data-end-date-fieldset-target=${#strings.endsWith(followUp.name, 'NoEndDate') ? input.followUps[followUpStatMember.index - 1].name : null}, data-member-index=${#strings.endsWith(followUp.name, 'NoEndDate') ? iterationStat.index : null}">
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
		<div th:if="!${#arrays.isEmpty(inputDataErrors)}">
			<p class="text--error" th:aria-label="#{error.title}" th:id="${input.name} + '-error-p'">
				<i class="icon-warning" th:id="${input.name + '-error-icon'}"></i>
				<span th:id="${input.name} + '-error-message-'" th:class="${input.name + '-error'}"
					th:text="#{${inputDataErrors[0]}}"></span>
			</p>
		</div>
		<div th:if="${inputFollowupsHasErrors && !noPersonsChecked}">
			<p class="text--error" th:aria-label="#{error.title}" th:id="${input.name} + '-error-p'">
				<i class="icon-warning" th:id="${input.name + '-error-icon'}"></i>
				<span th:id="${input.name} + '-error-message-'" th:class="${input.name + '-error'}"
					th:text="#{${inputFollowupDataErrors[0]}}"></span>
			</p>
		</div>
	</div>
	<!-- When "No end date" checkbox is checked, disable the end-date month/day/year inputs (and re-enable when unchecked). Works for applicant-only and with household members. -->
	<script th:if="${input.followUps != null}">
		(function() {
			function syncEndDateDisabled(checkbox) {
				var fieldset = null;
				// Applicant (or applicant-only): checkbox is inside a wrapper with data-no-end-date-checkbox; find the fieldset with that name and no data-end-date-member-index.
				var wrapper = checkbox.closest('[data-no-end-date-checkbox]');
				if (wrapper) {
					var name = wrapper.getAttribute('data-no-end-date-checkbox');
					if (name) {
						var sets = document.querySelectorAll('fieldset[data-end-date-fieldset="' + name + '"]');
						for (var i = 0; i < sets.length; i++) {
							if (!sets[i].hasAttribute('data-end-date-member-index')) { fieldset = sets[i]; break; }
						}
					}
				} else if (checkbox.hasAttribute('data-end-date-fieldset-target') && checkbox.hasAttribute('data-member-index')) {
					var target = checkbox.getAttribute('data-end-date-fieldset-target');
					var idx = checkbox.getAttribute('data-member-index');
					var sets = document.querySelectorAll('fieldset[data-end-date-fieldset="' + target + '"][data-end-date-member-index="' + idx + '"]');
					if (sets.length) fieldset = sets[0];
				}
				if (fieldset) {
					var inputs = fieldset.querySelectorAll('input[type="text"]');
					inputs.forEach(function(inp) { inp.disabled = checkbox.checked; });
				}
			}
			function run() {
				document.querySelectorAll('.no-end-date-checkbox').forEach(function(cb) {
					syncEndDateDisabled(cb);
				});
			}
			if (document.readyState === 'loading') {
				document.addEventListener('DOMContentLoaded', run);
			} else {
				run();
			}
			document.addEventListener('change', function(e) {
				if (e.target && e.target.classList && e.target.classList.contains('no-end-date-checkbox')) {
					syncEndDateDisabled(e.target);
				}
			});
		})();
	</script>
</th:block>
</html>
