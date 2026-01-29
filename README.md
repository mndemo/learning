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
