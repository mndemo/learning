<!DOCTYPE html>
<html th:lang="${#locale.language}" xmlns:th="http://www.thymeleaf.org">
<!--
  HOUSEHOLD OPTIONS CHECKBOXES WITH FOLLOW-UP
  Renders: (1) main checkbox list = which household members get this option (e.g. income type),
           (2) for each selected person, a follow-up block (amount, frequency, start date, end date, no end date).
  VALUE LAYOUT: Single-value (MONEY, SELECT, CHECKBOX) = value[0] applicant, value[1] 1st member, ...
  DATE = value[0,1,2] applicant month/day/year, value[3,4,5] 1st member, ...
  No end date CHECKBOX = NO_END_DATE_0 (applicant), NO_END_DATE_1 (1st member), ...
  Person index: applicant = 0, first household member = 1 (iterationStat.count), etc.
-->
<th:block th:fragment="householdOptionsCheckboxesWithFollowup (input, data)"
	th:with="
		formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
		inputData=${data.get(input.name)},
		inputDataNames=${data.get(input.name)},
		sortedHouseholdMembers=${inputDataNames != null and inputDataNames.value != null ? T(org.codeforamerica.shiba.pages.PageUtils).householdMemberSort(inputDataNames.value) : T(java.util.Collections).emptyList()},
		hasError=${inputData != null and !data.isValid() and !inputData.valid(data)},
		personalInfoPage=${(input.options != null and input.options.datasources != null and input.options.datasources.get('personalInfo') != null) ? input.options.datasources.get('personalInfo') : null},
		yourFullName=${personalInfoPage != null and personalInfoPage.get('firstName') != null and personalInfoPage.get('firstName').value != null and !personalInfoPage.get('firstName').value.isEmpty() and personalInfoPage.get('lastName') != null and personalInfoPage.get('lastName').value != null and !personalInfoPage.get('lastName').value.isEmpty() ? (personalInfoPage.get('firstName').value[0] + ' ' + personalInfoPage.get('lastName').value[0]) : 'You'},
		needsAriaLabel=${input.needsAriaLabel()},
		youIsChecked=${T(org.codeforamerica.shiba.pages.PageUtils).listOfNamesContainsName(sortedHouseholdMembers, yourFullName + ' applicant')},
		noPersonsChecked=${#lists.isEmpty(sortedHouseholdMembers)}
	">
	<div class="form-group" th:classappend="${hasError} ? 'form-group--error' : ''">
		<!-- APPLICANT ROW: "You" checkbox + follow-up inputs (person index 0) -->
		<div class="question-with-follow-up" style="margin-bottom: 1rem;">
			<div class="question-with-follow-up__question">
				<div class="form-group">
					<label th:for="householdMember-me" class="checkbox display-flex" style="margin-bottom: 1rem;">
						<input type="checkbox" th:id="householdMember-me"
							th:value="${yourFullName + ' applicant'}"
							th:name="${formInputName}" th:checked="${youIsChecked}"
							th:attrappend="data-follow-up=|#${input.name}-follow-up|">
						<span th:text="|${yourFullName} #{general.you}|"> </span>
					</label>
				</div>
			</div>
			<!-- Applicant follow-up: all follow-up inputs use value index 0 (single-value) or 0,1,2 (DATE) -->
			<div class="question-with-follow-up__follow-up" th:id="|${input.name}-follow-up|">
				<th:block th:each="applicantFollowUp: ${input.followUps != null ? input.followUps : T(java.util.Collections).emptyList()}" th:with="currentFollowupData=${data.get(applicantFollowUp.name)}, currentFollowupFormName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(applicantFollowUp.name)}">
					<div th:replace="~{fragments/form-question-prompt :: formQuestionPrompt(${applicantFollowUp})}"></div>
					<p class="text--help" th:id="${applicantFollowUp.name + '-help-message'}" th:if="${applicantFollowUp.helpMessageKey != null}"
						th:utext="#{${applicantFollowUp.helpMessageKey}}"></p>
					<th:block th:switch="${applicantFollowUp.type}">
						<!-- APPLICANT: MONEY - value[0] -->
						<div class="form-group" th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).MONEY}" th:classappend="${youIsChecked and currentFollowupData != null and !currentFollowupData.valid(data)} ? 'form-group--error' : ''">
							<div class="text-input-group">
								<div class="text-input-group__prefix" style="background-color:#FFFFFF">$</div>
								<input type="text" class="text-input"
									th:aria-describedby="${applicantFollowUp.helpMessageKey != null ? applicantFollowUp.name + '-help-message' : ''}"
									th:aria-labelledby="${needsAriaLabel ? applicantFollowUp.name + '-label' : ''}"
									th:aria-invalid="${hasError}"
									th:id="|${applicantFollowUp.name}|" th:name="${currentFollowupFormName}"
									th:value="${(currentFollowupData != null and currentFollowupData.value != null and #lists.size(currentFollowupData.value) > 0 and currentFollowupData.value[0] != null and !currentFollowupData.value[0].isEmpty()) ? currentFollowupData.value[0] : ''}">
								<div class="text-input-group__postfix" style="white-space: nowrap; background-color:#FFFFFF"
									th:if="${applicantFollowUp.inputPostfix != null}" th:text="#{${applicantFollowUp.inputPostfix}}"></div>
							</div>
						</div>
						<!-- APPLICANT: SELECT (e.g. frequency) - value[0] -->
						<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).SELECT}" class="form-group">
							<div class="select">
								<select th:id="${applicantFollowUp.name}" class="select__element"
									th:name="${currentFollowupFormName}"
									th:aria-invalid="${hasError}">
									<th:block th:each="option: ${applicantFollowUp.options != null and applicantFollowUp.options.selectableOptions != null ? applicantFollowUp.options.selectableOptions : T(java.util.Collections).emptyList()}">
										<option th:value="${option.value}" th:text="#{${option.messageKey}}"
											th:selected="${currentFollowupData != null and currentFollowupData.value != null and #lists.size(currentFollowupData.value) > 0 and currentFollowupData.value[0] == option.value}"></option>
									</th:block>
								</select>
							</div>
						</div>
						<!-- APPLICANT: DATE (start/end) - value[0]=month, value[1]=day, value[2]=year -->
						<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).DATE}" class="form-group"
							th:with="
								month=${(currentFollowupData != null and currentFollowupData.value != null and #lists.size(currentFollowupData.value) > 0 and currentFollowupData.value[0] != null and !currentFollowupData.value[0].isEmpty()) ? currentFollowupData.value[0] : ''},
								date=${(currentFollowupData != null and currentFollowupData.value != null and #lists.size(currentFollowupData.value) > 1 and currentFollowupData.value[1] != null and !currentFollowupData.value[1].isEmpty()) ? currentFollowupData.value[1] : ''},
								year=${(currentFollowupData != null and currentFollowupData.value != null and #lists.size(currentFollowupData.value) > 2 and currentFollowupData.value[2] != null and !currentFollowupData.value[2].isEmpty()) ? currentFollowupData.value[2] : ''}
							">
							<fieldset class="date-input">
								<p class="text--help">
									<label th:for="${applicantFollowUp.name + '-month'}" th:text="#{general.month}"></label>
									&nbsp;/&nbsp;
									<label th:for="${applicantFollowUp.name + '-day'}" th:text="#{general.day}"></label>
									&nbsp;/&nbsp;
									<label th:for="${applicantFollowUp.name + '-year'}" th:text="#{general.year}"></label>
								</p>
								<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
									th:id="${applicantFollowUp.name + '-month'}" th:name="${currentFollowupFormName}"
									th:value="${month}" th:placeholder="mm"/>
								&nbsp;/&nbsp;
								<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
									th:id="${applicantFollowUp.name + '-day'}" th:name="${currentFollowupFormName}"
									th:value="${date}" th:placeholder="dd"/>
								&nbsp;/&nbsp;
								<input type="text" inputmode="numeric" maxlength="4" class="text-input form-width--4-character dob-input"
									th:id="${applicantFollowUp.name + '-year'}" th:name="${currentFollowupFormName}"
									th:value="${year}" th:placeholder="yyyy"/>
							</fieldset>
						</div>
						<!-- APPLICANT: CHECKBOX (e.g. No end date) - value contains NO_END_DATE_0 -->
						<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).CHECKBOX}" class="form-group"
							th:with="
								message=${applicantFollowUp.options != null and applicantFollowUp.options.selectableOptions != null and !applicantFollowUp.options.selectableOptions.isEmpty() ? applicantFollowUp.options.selectableOptions.get(0).messageKey : 'general.yes'},
								checkboxValues=${currentFollowupData != null ? currentFollowupData.value : null},
								isChecked=${checkboxValues != null and checkboxValues.contains('NO_END_DATE_0')}
							">
							<label th:for="no_end_date_checkbox" class="checkbox">
								<input type="checkbox" th:id="no_end_date_checkbox"
									th:value="NO_END_DATE_0" th:name="${currentFollowupFormName}"
									th:checked="${isChecked}">
								<span th:utext="#{${message}}"></span>
							</label>
						</div>
					</th:block>
					<!-- Applicant follow-up errors: use standard fragment when applicant is checked and this follow-up has errors -->
					<th:block th:if="${youIsChecked}" th:with="inputData=${data.get(applicantFollowUp.name)}">
						<div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${applicantFollowUp})}"></div>
					</th:block>
				</th:block>
			</div>
		</div>

		<!-- HOUSEHOLD MEMBER ROWS: each member checkbox + follow-up (person index = iterationStat.count: 1, 2, 3, ...) -->
		<th:block th:each="iteration, iterationStat: ${input.options != null and input.options.subworkflows != null and input.options.subworkflows.get('household') != null ? input.options.subworkflows.get('household') : T(java.util.Collections).emptyList()}"
			th:with="
				hmInfo=${iteration.getPagesData() != null ? iteration.getPagesData().get('householdMemberInfo') : null},
				hmFirst=${hmInfo != null ? hmInfo.get('firstName') : null},
				hmLast=${hmInfo != null ? hmInfo.get('lastName') : null},
				fullName=${hmFirst != null and hmFirst.value != null and !hmFirst.value.isEmpty() and hmLast != null and hmLast.value != null and !hmLast.value.isEmpty() ? (hmFirst.value[0] + ' ' + hmLast.value[0]) : 'Household member'},
				memberIsChecked=${T(org.codeforamerica.shiba.pages.PageUtils).listOfNamesContainsName(sortedHouseholdMembers, fullName + ' ' + iteration.id)}
			">
			<div class="question-with-follow-up" style="margin-bottom: 1rem;">
				<div class="question-with-follow-up__question">
					<div class="form-group">
						<label th:for="|${formInputName}${iterationStat.index}|" class="checkbox display-flex">
							<input type="checkbox" th:id="|${formInputName}${iterationStat.index}|"
								th:value="${fullName + ' ' + iteration.id}" th:name="${formInputName}"
								th:checked="${memberIsChecked}"
								th:attrappend="data-follow-up=|#${input.name}${iterationStat.index}-follow-up|">
							<span th:text="${fullName}"></span>
						</label>
					</div>
				</div>
				<!-- Member follow-up: single-value inputs use value[iterationStat.count]; DATE uses value[count*3], value[count*3+1], value[count*3+2] -->
				<div class="question-with-follow-up__follow-up" th:id="|${input.name}${iterationStat.index}-follow-up|">
					<th:block th:each="followUp: ${input.followUps != null ? input.followUps : T(java.util.Collections).emptyList()}"
						th:with="
							currentFollowupData=${data.get(followUp.name)},
							currentFollowupFormName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(followUp.name)}
						">
						<div class="form-group" th:classappend="${memberIsChecked and currentFollowupData != null and !currentFollowupData.valid(data)} ? 'form-group--error' : ''">
							<div th:replace="~{fragments/form-question-prompt :: formQuestionPrompt(${followUp})}"></div>
							<p class="text--help" th:id="${followUp.name + iterationStat.index + '-help-message'}" th:if="${followUp.helpMessageKey != null}"
								th:utext="#{${followUp.helpMessageKey}}"></p>
							<th:block th:switch="${followUp.type}">
								<!-- MEMBER: MONEY - value[iterationStat.count] -->
								<div class="form-group" th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).MONEY}">
									<div class="text-input-group">
									<div class="text-input-group__prefix" style="background-color: #FFFFFF">$</div>
									<input type="text" class="text-input"
										th:aria-describedby="${followUp.helpMessageKey != null ? followUp.name + iterationStat.index + '-help-message' : ''}"
										th:aria-labelledby="${needsAriaLabel ? followUp.name + '-label' : ''}"
										th:aria-invalid="${hasError}"
										th:id="|${followUp.name}${iterationStat.index}|" th:name="${currentFollowupFormName}"
										th:value="${(currentFollowupData != null and currentFollowupData.value != null and iterationStat.count < currentFollowupData.value.size() and currentFollowupData.value[iterationStat.count] != null and !currentFollowupData.value[iterationStat.count].isEmpty()) ? currentFollowupData.value[iterationStat.count] : ''}">
									<div class="text-input-group__postfix" style="white-space: nowrap; background-color: #FFFFFF"
										th:if="${followUp.inputPostfix != null}" th:text="#{${followUp.inputPostfix}}"></div>
									</div>
								</div>
								<!-- MEMBER: SELECT - value[iterationStat.count] -->
								<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).SELECT}" class="form-group">
									<div class="select">
										<select th:id="|${followUp.name}${iterationStat.index}|" class="select__element"
											th:name="${currentFollowupFormName}"
											th:aria-invalid="${hasError}">
											<th:block th:each="option: ${followUp.options != null and followUp.options.selectableOptions != null ? followUp.options.selectableOptions : T(java.util.Collections).emptyList()}">
												<option th:value="${option.value}" th:text="#{${option.messageKey}}"
													th:selected="${currentFollowupData != null and currentFollowupData.value != null and iterationStat.count < currentFollowupData.value.size() and currentFollowupData.value[iterationStat.count] == option.value}"></option>
											</th:block>
										</select>
									</div>
								</div>
								<!-- MEMBER: DATE - value[count*3]=month, value[count*3+1]=day, value[count*3+2]=year -->
								<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).DATE}" class="form-group"
									th:with="
										month=${(currentFollowupData != null and currentFollowupData.value != null and #lists.size(currentFollowupData.value) > (iterationStat.count * 3) and currentFollowupData.value[iterationStat.count * 3] != null and !currentFollowupData.value[iterationStat.count * 3].isEmpty()) ? currentFollowupData.value[iterationStat.count * 3] : ''},
										date=${(currentFollowupData != null and currentFollowupData.value != null and #lists.size(currentFollowupData.value) > (iterationStat.count * 3 + 1) and currentFollowupData.value[iterationStat.count * 3 + 1] != null and !currentFollowupData.value[iterationStat.count * 3 + 1].isEmpty()) ? currentFollowupData.value[iterationStat.count * 3 + 1] : ''},
										year=${(currentFollowupData != null and currentFollowupData.value != null and #lists.size(currentFollowupData.value) > (iterationStat.count * 3 + 2) and currentFollowupData.value[iterationStat.count * 3 + 2] != null and !currentFollowupData.value[iterationStat.count * 3 + 2].isEmpty()) ? currentFollowupData.value[iterationStat.count * 3 + 2] : ''}
									">
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
											th:value="${month}" th:placeholder="mm"/>
										&nbsp;/&nbsp;
										<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
											th:id="|${followUp.name}${iterationStat.index}-day|" th:name="${currentFollowupFormName}"
											th:value="${date}" th:placeholder="dd"/>
										&nbsp;/&nbsp;
										<input type="text" inputmode="numeric" maxlength="4" class="text-input form-width--4-character dob-input"
											th:id="|${followUp.name}${iterationStat.index}-year|" th:name="${currentFollowupFormName}"
											th:value="${year}" th:placeholder="yyyy"/>
									</fieldset>
								</div>
								<!-- MEMBER: CHECKBOX (No end date) - value contains NO_END_DATE_1, NO_END_DATE_2, ... -->
								<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).CHECKBOX}" class="form-group"
									th:with="
										noEndMsgKey=${followUp.options != null and followUp.options.selectableOptions != null and !followUp.options.selectableOptions.isEmpty() ? followUp.options.selectableOptions.get(0).messageKey : 'general.yes'},
										checkboxValues=${currentFollowupData != null ? currentFollowupData.value : null},
										noEndVal=${'NO_END_DATE_' + iterationStat.count},
										isChecked=${checkboxValues != null and checkboxValues.contains(noEndVal)}
									">
									<label th:for="no_end_date_checkbox" class="checkbox">
										<input type="checkbox"
											th:id="|${followUp.name}${iterationStat.count}|"
											th:value="${noEndVal}"
											th:name="${currentFollowupFormName}"
											th:checked="${isChecked}">
										<span th:utext="#{${noEndMsgKey}}"></span>
									</label>
								</div>
							</th:block>
						</div>
						<!-- Member follow-up errors: use standard fragment when member is checked and this follow-up has errors -->
						<th:block th:if="${memberIsChecked}" th:with="inputData=${data.get(followUp.name)}">
							<div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${followUp})}"></div>
						</th:block>
					</th:block>
				</div>
			</div>
		</th:block>

		<!-- Main input error (e.g. "select at least one") -->
		<th:block th:with="inputData=${data.get(input.name)}">
			<div th:replace="~{fragments/inputErrorFragment :: validationError(${data}, ${input})}"></div>
		</th:block>
	</div>
</th:block>
</html>
