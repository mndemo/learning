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
