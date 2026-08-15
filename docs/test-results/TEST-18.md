\# TEST-18 – Manual AI Upload Testing



\## Test Environment



\- Project: ShoeShop

\- Branch: feat/TEST-18-manual-ai-upload-testing

\- AI Service: FastAPI

\- API: POST /api/v1/analyze

\- Environment: Docker Compose

\- Swagger: http://localhost:8000/docs



\## Test Cases



\### TC01 – Upload MOV file



\*\*Input:\*\* `.MOV` video file



\*\*Expected:\*\*

The API should reject files that are not supported image formats.



\*\*Actual:\*\*

\- HTTP Status: 200

\- approved: false

\- status: REJECTED

\- reason: Không thể đọc file ảnh. Vui lòng kiểm tra định dạng (JPG/PNG).



\*\*Result:\*\* PASS



\---



\### TC02 – Upload PNG image



\*\*Input:\*\* PNG image containing an umbrella



\*\*Expected:\*\*

The API should successfully process the PNG image.



\*\*Actual:\*\*

\- HTTP Status: 200

\- approved: true

\- status: APPROVED

\- blur\_score: 1756.58

\- is\_blurry: false

\- clutter\_score: 0.1397

\- is\_cluttered: false

\- num\_objects: 1

\- detected\_classes: umbrella



\*\*Result:\*\* PASS



\*\*Observation:\*\*

The image passed the quality checks. However, the detected object

was classified as `umbrella`, so this case is used to verify image

processing rather than shoe classification.



\---



\### TC03 – Upload valid shoe image



\*\*Input:\*\* PNG image containing a shoe



\*\*Expected:\*\*

The API should successfully process a valid product image.



\*\*Actual:\*\*

\- HTTP Status: 200

\- approved: true

\- status: APPROVED

\- reason: Ảnh đạt tiêu chuẩn chất lượng.

\- blur\_score: 70.6

\- blur\_threshold: 50

\- is\_blurry: false

\- clutter\_score: 0.0072

\- is\_cluttered: false

\- num\_objects: 1

\- detected\_classes: product\_item

\- image\_size: 503x500



\*\*Result:\*\* PASS



\---



\## Test Summary



| Test Case | Description | HTTP | Status | Result |

|---|---|---:|---|---|

| TC01 | Upload MOV file | 200 | REJECTED | PASS |

| TC02 | Upload PNG image | 200 | APPROVED | PASS |

| TC03 | Upload valid shoe image | 200 | APPROVED | PASS |



\## Conclusion



Manual AI upload testing was completed successfully.



The API correctly rejected the unsupported MOV file and successfully

processed PNG images. The valid shoe image was approved with

`product\_item` detected as the product class.

