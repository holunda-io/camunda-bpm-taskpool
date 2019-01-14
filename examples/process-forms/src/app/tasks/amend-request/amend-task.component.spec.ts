import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import 'rxjs/add/observable/of';
import { AmendTaskComponent } from './amend-task.component';
import { Observable } from 'rxjs-compat';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AmendRequestService } from 'process/api/amendRequest.service';


describe('Component: ApproveTaskComponent', () => {

  const taskId = '4711';
  let component: AmendTaskComponent;
  let fixture: ComponentFixture<AmendTaskComponent>;

  beforeEach(async(() => {

    const amendRequestServiceSpy = jasmine.createSpyObj('AmendRequestService', {
      'loadTaskAmendRequestFormData': Observable.of({
        approvalRequest: {},
        task: {},
      }),
      'submitTaskAmendRequestSubmitData': Observable.of({}),
    });

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        AmendTaskComponent,
      ],
      providers: [
        { provide: AmendRequestService, useValue: amendRequestServiceSpy },
        { provide: ActivatedRoute, useValue: {
            snapshot: {
              paramMap: {get: () => taskId},
            }
          },
        },
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(AmendTaskComponent);

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
