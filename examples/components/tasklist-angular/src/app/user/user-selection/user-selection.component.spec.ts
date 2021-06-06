import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {UserSelectionComponent} from './user-selection.component';
import {provideStoreServiceMock} from '@ngxp/store-service/testing';
import {UserStoreService} from 'app/user/state/user.store-service';

describe('UserSelectionComponent', () => {
  let component: UserSelectionComponent;
  let fixture: ComponentFixture<UserSelectionComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ UserSelectionComponent ],
      providers: [
        provideStoreServiceMock(UserStoreService, {
          currentUserProfile$: {
            userIdentifier: '',
            username: '',
            fullName: ''
          }
        })
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UserSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
