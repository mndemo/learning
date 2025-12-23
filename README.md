<th:block th:if="${page.hasPreFormContentFragment()}">
          <div class="grid__item spacing-below-35">
            <div th:replace="~{|fragments/${page.preFormContentFragment}| :: ${page.preFormContentFragment}}"></div>
          </div>
        </th:block>
