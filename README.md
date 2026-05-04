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
  /* Place mn.gov logo and the mobile build-version on a single row, vertically
     centered, with the build-version pinned to the far right. */
  .main-footer__mn-logo {
    display: flex !important;     /* override any inline/block from .grid__item */
    flex-wrap: nowrap;
    align-items: center;          /* horizontally aligned (same baseline as logo) */
    justify-content: space-between;
    width: 100%;
    gap: 1rem;
  }
  .main-footer__mn-logo > .illustration--mn-logo {
    flex: 0 0 auto;               /* keep logo at its intrinsic 70×43 size */
  }
  .main-footer__mn-logo > .footer-build-version--mobile {
    margin: 0 0 0 auto;           /* push to far right */
    text-align: right;
    white-space: nowrap;
    line-height: 1;               /* tighter so it visually sits at logo center */
  }
}
