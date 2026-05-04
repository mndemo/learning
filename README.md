/* Footer build-version label. Two copies in the markup, toggled via media query.
   Desktop (>=601px): full-width line at the very bottom of the footer (below the floated
   mn.gov logo), right-aligned so it sits underneath the logo column.
   Mobile (<=600px): inline with the mn.gov logo on the same row, pushed to the far right. */
.footer-build-version {
  color: #cdcdcd;
  font-size: 0.875rem;
  margin: 0;
}
@media screen and (min-width: 601px) {
  .footer-build-version--mobile {
    display: none;
  }
  .footer-build-version--desktop {
    clear: both;            /* drop below the floated .main-footer__mn-logo */
    margin-top: 1.5rem;
    text-align: right;      /* sit visually under the logo on the right */
  }
}
@media screen and (max-width: 600px) {
  .footer-build-version--desktop {
    display: none;
  }
  /* Lay out the mn.gov logo and the mobile build-version on a single row,
     logo on the left and build-version pushed to the far right. */
  .main-footer__mn-logo {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
  }
  .footer-build-version--mobile {
    margin-left: auto;      /* belt-and-braces against any flex shrinkage */
    text-align: right;
    white-space: nowrap;
  }
}
