import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {DataentryListComponent} from 'app/dataentry/dataentry-list/dataentry-list.component';
import {FormsModule} from '@angular/forms';

import {provideStoreServiceMock} from '@ngxp/store-service/testing';
import {DataentryStoreService} from 'app/dataentry/state/dataentry.store-service';
import {SharedModule} from 'app/shared/shared.module';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {UserStoreService} from 'app/user/state/user.store-service';

describe('Component: DataentrylistComponent', () => {

  let component: DataentryListComponent;
  let fixture: ComponentFixture<DataentryListComponent>;

  beforeEach(waitForAsync(() => {

    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        SharedModule,
        NgbModule,
      ],
      declarations: [
        DataentryListComponent
      ],
      providers: [
        provideStoreServiceMock(DataentryStoreService, {
          dataEntries$: []
        }),
        provideStoreServiceMock(UserStoreService)
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(DataentryListComponent);

      // get test component from the fixture
      component = fixture.componentInstance;

      // detect changes
      fixture.detectChanges();
    });
  }));

  it('should create', () => {
    expect(component).toBeDefined();
  });

});
