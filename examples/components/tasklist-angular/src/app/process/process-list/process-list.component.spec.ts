import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {ProcesslistComponent} from './process-list.component';
import {FormsModule} from '@angular/forms';
import {provideStoreServiceMock} from '@ngxp/store-service/testing';
import {ProcessStoreService} from 'app/process/state/process.store-service';

describe('Component: TasklistComponent', () => {

  let component: ProcesslistComponent;
  let fixture: ComponentFixture<ProcesslistComponent>;

  beforeEach(waitForAsync(() => {

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        ProcesslistComponent
      ],
      providers: [
        provideStoreServiceMock(ProcessStoreService)
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(ProcesslistComponent);

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
