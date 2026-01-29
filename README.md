<th:block th:each="youFollowUp, followUpStat : ${input.followUps}"
					th:with="currentFollowupData=${data.get(youFollowUp.name)},
					         currentFollowupFormName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(youFollowUp.name)}">
				68-70

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
									th:placeholder="mm"/>
								&nbsp;/&nbsp;
								<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
									th:id="${youFollowUp.name}+'-day'" th:name="${currentFollowupFormName}"
									th:value="${(#lists.size(currentFollowupData.value) > 1) ? currentFollowupData.value[1] : ''}"
									th:placeholder="dd"/>
								&nbsp;/&nbsp;
								<input type="text" inputmode="numeric" maxlength="4" class="text-input form-width--4-character dob-input"
									th:id="${youFollowUp.name}+'-year'" th:name="${currentFollowupFormName}"
									th:value="${(#lists.size(currentFollowupData.value) > 2) ? currentFollowupData.value[2] : ''}"
									th:placeholder="yyyy"/>
							</fieldset>

							110-134

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

						134-144

							th:with="currentFollowupData=${data.get(followUp.name)},
						         currentFollowupFormName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(followUp.name)}">
				173-174

				<div th:case="${T(org.codeforamerica.shiba.pages.config.FormInputType).DATE}" class="form-group">
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
											th:placeholder="mm"/>
										&nbsp;/&nbsp;
										<input type="text" inputmode="numeric" maxlength="2" class="text-input form-width--2-character dob-input"
											th:id="|${followUp.name}${iterationStat.index}-day|" th:name="${currentFollowupFormName}"
											th:value="${(iterationStat.count*3 + 1 < currentFollowupData.value.size()) ? currentFollowupData.value[iterationStat.count*3 + 1] : ''}"
											th:placeholder="dd"/>
										&nbsp;/&nbsp;
										<input type="text" inputmode="numeric" maxlength="4" class="text-input form-width--4-character dob-input"
											th:id="|${followUp.name}${iterationStat.index}-year|" th:name="${currentFollowupFormName}"
											th:value="${(iterationStat.count*3 + 2 < currentFollowupData.value.size()) ? currentFollowupData.value[iterationStat.count*3 + 2] : ''}"
											th:placeholder="yyyy"/>
									</fieldset>
								</div>

								212 - 235

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
