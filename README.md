<th:block th:each="option, optionStat : ${input.options.selectableOptions}"
                          th:with="formInputName=${T(org.codeforamerica.shiba.pages.PageUtils).getFormInputName(input.name)},
                                  optionId=${input.name + '-' + optionStat.index}">
