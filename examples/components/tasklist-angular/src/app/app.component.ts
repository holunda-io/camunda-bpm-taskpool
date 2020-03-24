import {Component, OnInit} from '@angular/core';
import {UserStoreService} from 'app/user/state/user.store-service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['app.component.scss']
})
export class AppComponent implements OnInit {

  constructor(private userStore: UserStoreService) {
  }

  ngOnInit(): void {
    this.userStore.loadInitialUser();
  }
}
