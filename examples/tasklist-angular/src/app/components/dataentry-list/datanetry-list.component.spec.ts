import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DataentryListComponent} from 'src/app/components/dataentry-list/dataentry-list.component';
import {FormsModule} from '@angular/forms';

import {FilterService} from 'app/services/filter.service';
import {provideStoreServiceMock} from '@ngxp/store-service/testing';
import {DataentryStoreService} from 'app/dataentry/state/dataentry.store-service';

describe('Component: DataentrylistComponent', () => {

  let component: DataentryListComponent;
  let fixture: ComponentFixture<DataentryListComponent>;

  beforeEach(async(() => {

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        DataentryListComponent
      ],
      providers: [
        FilterService,
        provideStoreServiceMock(DataentryStoreService)
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
