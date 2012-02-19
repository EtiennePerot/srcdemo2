@rd /S /Q build
@rd /S /Q dist
@rd /S /Q precompiled
@python -OO setup.py
@ren dist precompiled
@rd /S /Q build
@pause
