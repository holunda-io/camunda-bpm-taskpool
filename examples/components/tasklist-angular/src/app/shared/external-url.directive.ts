import {Directive, ElementRef, HostListener, OnDestroy} from '@angular/core';
import {Router} from '@angular/router';
import {UserStoreService} from 'app/user/state/user.store-service';
import {Subscription} from 'rxjs';

@Directive({
  selector: 'a[appExternalUrl]',
})
export class ExternalUrlDirective implements OnDestroy {

  private userId: string;
  private _sub: Subscription;

  constructor(private el: ElementRef, private router: Router, private userStore: UserStoreService) {
    this._sub = this.userStore.userId$().subscribe(userId => this.userId = userId);
  }

  ngOnDestroy(): void {
    this._sub.unsubscribe();
  }

  @HostListener('click', ['$event'])
  clicked(event: Event) {
    const url = this.el.nativeElement.href;
    if (url === undefined || url === '') {
      return;
    }
    const parsedUrl = url.replace('%userId%', this.userId);

    this.router.navigate(['/externalRedirect', {externalUrl: parsedUrl}], {
      skipLocationChange: true,
    });

    event.preventDefault();
  }
}
